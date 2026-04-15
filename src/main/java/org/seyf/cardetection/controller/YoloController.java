package org.seyf.cardetection.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opencv.core.Mat;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.seyf.cardetection.model.Camera;
import org.seyf.cardetection.model.Slot;
import org.seyf.cardetection.service.CameraService;
import org.seyf.cardetection.service.SlotImportService;
import org.seyf.cardetection.service.SlotService;
import org.seyf.cardetection.service.YoloService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

@RestController
@AllArgsConstructor
public class YoloController {

    private final YoloService yoloService;
    private final SlotImportService slotImportService;

    /*@GetMapping("/identify")
    public ResponseEntity<String> detectCars() {

        // Define your paths
        String inputPath = "src/main/resources/images/cam2_reference.png";

        // We'll save it in the same folder, but append "processed_" to the filename
        String outputPath = "src/main/resources/images/processed_cam2_reference.png";

        Mat img = Imgcodecs.imread(inputPath);

        if (img.empty()) {
            return ResponseEntity.badRequest().body("Error: Image could not be loaded. Check path.");
        }

        Camera camera = Camera.builder().id("cam2").build();
        // Send it to the service to be processed and saved
        String savedLocation = yoloService.processAndSaveCars(img,camera,outputPath);

        // Return a clean success message
        return ResponseEntity.ok("Success! Cars detected and image saved to: " + savedLocation);
    }
*/

    @PostMapping("/save")
    public ResponseEntity<String> saveSlots(@RequestBody JsonNode input) {
        try {
            slotImportService.importSlots(input);
            return ResponseEntity.ok("Slots saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to save slots: " + e.getMessage());
        }
    }

    @PostMapping("/saveMap")
    public ResponseEntity<String> saveMapSlots(@RequestBody JsonNode input) {
        try {
            slotImportService.insertMapSlotsCoordinates(input);
            return ResponseEntity.ok("Slots saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to save slots: " + e.getMessage());
        }
    }

}
