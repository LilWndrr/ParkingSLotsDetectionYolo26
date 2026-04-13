package org.seyf.cardetection.helper;


import lombok.AllArgsConstructor;
import org.seyf.cardetection.model.Slot;
import org.seyf.cardetection.service.SlotService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class CacheWarming {

    private final SlotService slotService;
    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void warmUpCache() {
        System.out.println("--- Starting Redis Cache Warming ---");

        List<Slot> slots = slotService.getAll();

        Map<String, Map<String, Object>> groupedByCamera = new HashMap<>();

        for (Slot slot : slots) {

            // 1. Fetch metadata from the relational entities
            String parkingName = slot.getLevel().getParking().getName();
            String levelId = slot.getLevel().getName(); // Assuming '0' was saved as the ID or Name
            String cameraId = slot.getCamera().getName(); // Assuming 'cam1' was saved as the ID or Name

            // 2. Construct the EXACT same key YoloService uses
            String cameraKey = "parking:" + parkingName + ":ground_level:" + levelId + ":camera:" + cameraId + ":slots";

            // 3. Use the human-readable Slot Name (e.g., "A1") instead of the UUID
            String slotField = slot.getName();

            // 4. Group them up
            groupedByCamera
                    .computeIfAbsent(cameraKey, k -> new HashMap<>())
                    .put(slotField, slot.isEmpty());
        }

        // 5. Batch push to Redis
        groupedByCamera.forEach((cameraKey, slotMap) -> {
            redisTemplate.opsForHash().putAll(cameraKey, slotMap);
            System.out.println("Pushed " + slotMap.size() + " slots to Redis key: " + cameraKey);
        });

        System.out.println("--- End of Caching: Loaded " + slots.size() + " slots ---");
    }
}