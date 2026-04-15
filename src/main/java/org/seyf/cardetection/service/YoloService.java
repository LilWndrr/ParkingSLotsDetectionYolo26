package org.seyf.cardetection.service;

import ai.onnxruntime.*;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.seyf.cardetection.model.Camera;
import org.seyf.cardetection.model.Message;
import org.seyf.cardetection.model.Slot;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service

public class YoloService {

    private final SlotService slotService;
    private final CameraService cameraService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    // --- TIER 2 CACHE: JVM Memory for static geometric data ---
    private final Map<String, List<Slot>> cameraSlotCache = new ConcurrentHashMap<>();

    private static class SlotTransition {
        Boolean candidateState = null;
        int count = 0;
    }
    private final Map<String, SlotTransition> transitionCache = new ConcurrentHashMap<>();
    private static final int REQUIRED_CONSECUTIVE_FRAMES = 3;


    private static final float CONFIDENCE_THRESHOLD = 0.35f;
    private static final int   INPUT_SIZE            = 640;
    private static final int   CAR_CLASS_ID          = 2;
    private static final int TRUCK_CLASS_ID = 7;
    private static final int BUS_CLASS_ID = 5;




    private OrtEnvironment env;
    private OrtSession     session;

    public YoloService(SlotService slotService, CameraService cameraService, RedisTemplate<String, Object> redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.slotService = slotService;
        this.cameraService = cameraService;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            ClassPathResource modelResource = new ClassPathResource("yolo26xSmall.onnx");

            File tempModelFile = File.createTempFile("yolo26xSmall", ".onnx");
            tempModelFile.deleteOnExit();

            try (InputStream inputStream = modelResource.getInputStream()) {
                Files.copy(inputStream, tempModelFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
            session = env.createSession(tempModelFile.getAbsolutePath(), options);

            System.out.println("YOLO26 model loaded successfully via ONNX Runtime.");

        } catch (Exception e) {
            System.err.println("Failed to load YOLO26 model: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @RabbitListener(queues = "${queue.name}",concurrency = "2-4")
    public void processAndSaveCars(@Payload byte[] imageBytes,
                                   @Header("camera_id") String cameraId,
                                   @Header("timestamp") Long timestamp,
                                   @Header("parking_name") String parkingName,
                                   @Header("ground_level_id") String groundLevel) {

        long startTime = System.nanoTime();

        if (session == null) {
            throw new IllegalStateException("ONNX session is not initialized.");
        }

        // 1. LAZY LOAD SLOTS (Hits database ONCE per camera, then caches in RAM forever)
        List<Slot> slots = cameraSlotCache.computeIfAbsent(cameraId, id -> {
            System.out.println("Fetching static slots for Camera " + id + " from PostgreSQL...");
            Camera cam = cameraService.getByName(id)
                    .orElseThrow(() -> new IllegalStateException("Camera not found: " + id));
            return slotService.getByCamera(cam).orElse(Collections.emptyList());
        });

        // 2. DECLARE NATIVE OBJECTS OUTSIDE TRY BLOCK
        MatOfByte matOfByte = null;
        Mat frame = null;
        Mat resized = null;
        Mat rgb = null;
        OnnxTensor inputTensor = null;
        OrtSession.Result result = null;

        Map<Slot, MatOfPoint2f> slotContours = new HashMap<>();
        Map<Slot, MatOfPoint> slotMats = new HashMap<>();

        try {
            matOfByte = new MatOfByte(imageBytes);
            frame = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);

            int imgWidth = frame.cols();
            int imgHeight = frame.rows();
            float scaleX = (float) imgWidth / INPUT_SIZE;
            float scaleY = (float) imgHeight / INPUT_SIZE;

            // 3. PRE-CALCULATE CONTOURS (Standard Loop to avoid lambda scope issues)
            for (Slot slot : slots) {
                org.opencv.core.Point[] pts = slot.getPoints().stream()
                        .map(p -> new org.opencv.core.Point(
                                p.get(0) / 100.0 * imgWidth,
                                p.get(1) / 100.0 * imgHeight
                        ))
                        .toArray(org.opencv.core.Point[]::new);
                slotContours.put(slot, new MatOfPoint2f(pts));
                slotMats.put(slot, new MatOfPoint(pts));
            }

            // 4. PREPROCESS IMAGE
            resized = new Mat();
            Imgproc.resize(frame, resized, new Size(INPUT_SIZE, INPUT_SIZE));
            rgb = new Mat();
            Imgproc.cvtColor(resized, rgb, Imgproc.COLOR_BGR2RGB);
            rgb.convertTo(rgb, CvType.CV_32F, 1.0 / 255.0);

            // 5. RUN INFERENCE
            float[] inputData = matToNCHW(rgb); // Make sure this method exists in your class
            long[] inputShape = {1, 3, INPUT_SIZE, INPUT_SIZE};
            String inputName = session.getInputNames().iterator().next();

            inputTensor = OnnxTensor.createTensor(env, java.nio.FloatBuffer.wrap(inputData), inputShape);
            result = session.run(Collections.singletonMap(inputName, inputTensor));

            // 6. PARSE OUTPUT
            float[][][] output = (float[][][]) result.get(0).getValue();
            int numDetections = output[0].length;
            int detectionCount = 0;
            List<org.opencv.core.Point> carGroundPoints = new ArrayList<>();
            List<Point[]> carPoints = new ArrayList<>();
            for (int i = 0; i < numDetections; i++) {
                float confidence = output[0][i][4];
                if (confidence < CONFIDENCE_THRESHOLD) continue;
                int classId = (int) output[0][i][5];
                if (classId == CAR_CLASS_ID||classId==TRUCK_CLASS_ID||classId ==BUS_CLASS_ID) {

                float x1 = output[0][i][0];
                float y1 = output[0][i][1];
                float x2 = output[0][i][2];
                float y2 = output[0][i][3];

                int left = clamp((int) (x1 * scaleX), 0, imgWidth);
                int top = clamp((int) (y1 * scaleY), 0, imgHeight);
                int right = clamp((int) (x2 * scaleX), 0, imgWidth);
                int bottom = clamp((int) (y2 * scaleY), 0, imgHeight);

                int boxWidth = right - left;
                int boxHeight = bottom - top;
                if (boxWidth <= 0 || boxHeight <= 0) continue;

                double centerX = left + (double) boxWidth / 2;
                double centerY = bottom - (boxHeight * 0.15);



                carGroundPoints.add(new org.opencv.core.Point(centerX, centerY));
                Imgproc.circle(frame, new org.opencv.core.Point(centerX, centerY), 3, new Scalar(0, 0, 255), -1);
                detectionCount++;}
            }



            String redisKey ="parking:" +parkingName + ":ground_level:" + groundLevel + ":camera:" + cameraId + ":slots";
            Map<Object, Object> currentRedisState = redisTemplate.opsForHash().entries(redisKey);
            Map<String, Object> stateChanges = new HashMap<>();

            for (Slot slot : slots) {
                MatOfPoint2f contour = slotContours.get(slot);




                boolean isDetectedOccupied = carGroundPoints.stream()
                        .anyMatch(carPt -> Imgproc.pointPolygonTest(contour, carPt, false) >= 0);

                boolean isDetectedEmpty = !isDetectedOccupied;
                String slotField = slot.getName();
                String transitionKey = cameraId + "_" + slotField;

                // Look up the OFFICIAL state in Redis
                Object previousStateObj = currentRedisState.get(slotField);
                boolean officialIsEmpty = (previousStateObj != null) ? (Boolean) previousStateObj : true;

                // Get or create the transition tracker for this specific slot
                SlotTransition transition = transitionCache.computeIfAbsent(transitionKey, k -> new SlotTransition());

                if (isDetectedEmpty == officialIsEmpty) {
                    // 1. Detection matches official state. Everything is stable. Reset counter.
                    transition.count = 0;
                    transition.candidateState = null;
                } else {
                    // 2. Detection differs from official state!
                    if (transition.candidateState != null && transition.candidateState == isDetectedEmpty) {
                        // The streak continues...
                        transition.count++;
                    } else {
                        // First time we are seeing this new state (start the streak)
                        transition.candidateState = isDetectedEmpty;
                        transition.count = 1;
                    }

                    // 3. Did we hit the 5-frame threshold?
                    if (transition.count >= REQUIRED_CONSECUTIVE_FRAMES) {
                        stateChanges.put(slotField, isDetectedEmpty); // Queue for Redis batch update
                        System.out.println("State Change Confirmed (" + REQUIRED_CONSECUTIVE_FRAMES + " frames) -> Slot " + slotField + " is now " + (isDetectedEmpty ? "EMPTY" : "FULL"));

                        // Update our local official state so the drawing logic below updates instantly
                        officialIsEmpty = isDetectedEmpty;


                        Message message = Message.builder().slotId(slotField).groundLevelName(groundLevel).parkingName(parkingName).isEmpty(officialIsEmpty).build();
                        messagingTemplate.convertAndSend("/topic/parking-updates",message);
                        // Reset tracker now that the state has successfully changed
                        transition.count = 0;
                        transition.candidateState = null;
                    }


                }



                Scalar color = officialIsEmpty ? new Scalar(0, 255, 0) : new Scalar(0, 0, 255);
                Imgproc.polylines(frame, List.of(slotMats.get(slot)), true, color, 1);


            }

            // 8. BATCH UPDATE REDIS
            if (!stateChanges.isEmpty()) {
                redisTemplate.opsForHash().putAll(redisKey, stateChanges);

                // 9. SAVE IMAGE BEFORE CLEANUP
                String destinationPath = "src/main/resources/images/" + cameraId + "_" + timestamp + ".jpg";
                Imgcodecs.imwrite(destinationPath, frame);
                System.out.println("Saved to: " + destinationPath);
                System.out.println("Detected " + detectionCount + " car(s).");
            }



        } catch (Exception e) {
            System.err.println("Inference error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 10. GUARANTEED NATIVE MEMORY CLEANUP
            if (matOfByte != null) matOfByte.release();
            if (frame != null) frame.release();
            if (resized != null) resized.release();
            if (rgb != null) rgb.release();

            for (MatOfPoint2f contour : slotContours.values()) {
                if (contour != null) contour.release();
            }
            for (MatOfPoint mat : slotMats.values()) {
                if (mat != null) mat.release();
            }

            if (inputTensor != null) {
                try { inputTensor.close(); } catch (Exception ignored) {}
            }
            if (result != null) {
                try { result.close(); } catch (Exception ignored) {}
            }

            long spendedTime= System.nanoTime()-startTime;

            long delay = System.currentTimeMillis() - timestamp;
            if (delay > 5000) {
                System.out.println("Frame is " + delay + "ms old. Dropping to catch up!");
                return;
            }
            System.out.println(spendedTime);
        }
    }
    private float[] matToNCHW(Mat mat) {
        int h = mat.rows();
        int w = mat.cols();
        int c = mat.channels();

        float[] nchw = new float[c * h * w];
        float[] hwc  = new float[h * w * c];
        mat.get(0, 0, hwc);

        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                for (int ch = 0; ch < c; ch++) {
                    nchw[ch * h * w + row * w + col] = hwc[(row * w + col) * c + ch];
                }
            }
        }
        return nchw;
    }



    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public SlotService getSlotService() {
        return slotService;
    }
}