package org.seyf.cardetection.helper;


import lombok.AllArgsConstructor;
import org.seyf.cardetection.model.Slot;
import org.seyf.cardetection.service.SlotService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class CacheWarming {
    private final SlotService slotService;
    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        System.out.println("--- Starting Redis Cache Warming ---");

        List<Slot> slots = slotService.getAll();

        Map<String, Map<String, Object>> groupedByCamera = new HashMap<>();

        for (Slot slot : slots) {
            String cameraKey = "camera:" + slot.getCamera().getId() + ":slots";


            String slotField = String.valueOf(slot.getId());


            groupedByCamera
                    .computeIfAbsent(cameraKey, k -> new HashMap<>())
                    .put(slotField, slot.isEmpty());
        }


        groupedByCamera.forEach((cameraKey, slotMap) -> {
            redisTemplate.opsForHash().putAll(cameraKey, slotMap);
        });

        System.out.println("--- End of Caching: Loaded " + slots.size() + " slots ---");
    }

}
