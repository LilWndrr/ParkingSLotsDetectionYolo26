package org.seyf.cardetection.service;

import tools.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.seyf.cardetection.model.Camera;
import org.seyf.cardetection.model.Slot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class SlotImportService {

    private final CameraService cameraService;
    private final SlotService slotService;

    @Transactional
    public void importSlots(JsonNode input) {
        input.forEach(node -> {

            // 1. Get or create Camera dynamically based on the JSON
            String cameraId = node.get("camera_id").asString();
            Camera camera = cameraService.getCamera(cameraId)
                    .orElseGet(() -> {
                        Camera newCamera = Camera.builder().id(cameraId).build();
                        cameraService.save(newCamera);
                        return newCamera;
                    });

            // 2. Extract points List<List<Double>> from JSON array of arrays
            List<List<Double>> points = new ArrayList<>();
            node.get("points").forEach(pointNode -> {
                List<Double> coordinate = new ArrayList<>();
                pointNode.forEach(coord -> coordinate.add(coord.asDouble()));
                points.add(coordinate);
            });

            // 3. Build and save Slot using the exact ID from the JSON
            Slot slot = Slot.builder()
                    .id(node.get("id").asString())
                    .camera(camera)
                    .isEmpty(node.get("isEmpty").asBoolean())
                    .original_height(node.get("original_height").asInt())
                    .original_width(node.get("original_width").asInt())
                    .points(points)
                    .build();

            slotService.addSlot(slot);
        });
    }
}