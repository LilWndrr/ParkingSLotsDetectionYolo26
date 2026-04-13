package org.seyf.cardetection.service;

import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.model.Parking;
import tools.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.seyf.cardetection.model.Camera;
import org.seyf.cardetection.model.Slot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SlotImportService {

    private final CameraService cameraService;
    private final SlotService slotService;
    private final ParkingService parkingService;
    private final GroundLevelService groundLevelService;

    @Transactional
    public void importSlots(JsonNode input) {

        // 1. TIER 1 BATCH CACHE (Prevents database hammering)
        Map<String, Parking> parkingCache = new HashMap<>();
        Map<String, GroundLevel> levelCache = new HashMap<>();
        Map<String, Camera> cameraCache = new HashMap<>();

        input.forEach(node -> {


            String parkingName = node.get("parking_name").asString();

            // 2. SMART LOOKUP: Check RAM first. If not in RAM, check DB. If not in DB, create it.
            Parking parking = parkingCache.computeIfAbsent(parkingName, name ->
                    parkingService.getByName(name).orElseGet(() -> {
                        Parking newParking = Parking.builder().name(name).build();
                        return parkingService.save(newParking); // Assuming save() returns the saved entity
                    })
            );

            String groundLevelName = node.get("level_id").asString();
            GroundLevel level = levelCache.computeIfAbsent(groundLevelName, name ->
                    groundLevelService.getByName(name).orElseGet(() -> {
                        GroundLevel newLevel = GroundLevel.builder().name(name).parking(parking).build();
                        return groundLevelService.save(newLevel);
                    })
            );

            String cameraName = node.get("camera_id").asString();
            Camera camera = cameraCache.computeIfAbsent(cameraName, name ->
                    cameraService.getCamera(name).orElseGet(() -> {
                        Camera newCamera = Camera.builder().level(level).name(name).build();
                        return cameraService.save(newCamera);
                    })
            );

            // 3. Extract points safely
            List<List<Double>> points = new ArrayList<>();
            node.get("points").forEach(pointNode -> {
                List<Double> coordinate = new ArrayList<>();
                pointNode.forEach(coord -> coordinate.add(coord.asDouble()));
                points.add(coordinate);
            });


            String slotName = node.get("name").asString();
            Slot slot = slotService.getByNameAndLevel(slotName, level).orElse(new Slot());


            slot.setName(slotName);
            slot.setCamera(camera);
            slot.setLevel(level);
            slot.setEmpty(node.get("isEmpty").asBoolean());
            slot.setOriginal_height(node.get("original_height").asInt());
            slot.setOriginal_width(node.get("original_width").asInt());
            slot.setPoints(points);

            slotService.addSlot(slot); // Save or Update
        });
    }
}