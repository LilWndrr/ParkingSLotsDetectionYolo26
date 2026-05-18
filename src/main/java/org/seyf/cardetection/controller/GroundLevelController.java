package org.seyf.cardetection.controller;

import lombok.RequiredArgsConstructor;
import org.seyf.cardetection.dto.GroundLevelResponseDto;
import org.seyf.cardetection.dto.SlotHeatValue;
import org.seyf.cardetection.dto.SlotMapDto;
import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.model.Slot;
import org.seyf.cardetection.repository.SlotEventRepository;
import org.seyf.cardetection.service.CloudStorageService;
import org.seyf.cardetection.service.GroundLevelService;
import org.seyf.cardetection.service.SlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/groundLevel")
@RequiredArgsConstructor
public class GroundLevelController {

    private final GroundLevelService groundLevelService;
    private final SlotService slotService;
    private final SlotEventRepository slotEventRepository;
    private final CloudStorageService cloudStorageService;

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        List<GroundLevelResponseDto> dtoList = groundLevelService.getAll().stream()
                .map(GroundLevelResponseDto::toDto).toList();
        if (dtoList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/get")
    public ResponseEntity<?> get(@RequestParam("level_id") String levelId) {
        GroundLevelResponseDto levelResponseDto = GroundLevelResponseDto
                .toDto(groundLevelService.get(levelId).orElse(null));
        if (levelResponseDto.getId() == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(levelResponseDto);
    }

    @GetMapping("/byParking")
    public ResponseEntity<?> getByParkingId(@RequestParam("parking_id") String id){
        List<GroundLevelResponseDto> levels =  groundLevelService.getByParkingId(id)
                .stream().map(GroundLevelResponseDto::toDto).toList();
        if(levels.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(levels);
    }

    /**
     * Returns all slots with their mapPoints polygon and occupancy heat intensity.
     * Used by the frontend spatial heatmap overlay.
     *
     * GET /api/v1/groundLevel/heatmap?level_id=abc-123
     */
    @GetMapping("/heatmap")
    public ResponseEntity<?> getHeatmap(@RequestParam("level_id") String levelId) {
        GroundLevel level = groundLevelService.get(levelId).orElse(null);
        if (level == null) {
            return ResponseEntity.notFound().build();
        }

        // Get all slots with their mapPoints
        List<Slot> slots = slotService.getByGrounfLevel(level);
        if (slots.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Get total transitions per slot name
        List<SlotHeatValue> heatValues = slotEventRepository.sumTransitionsPerSlot(level);
        Map<String, Long> transitionMap = heatValues.stream()
                .collect(Collectors.toMap(SlotHeatValue::getSlotName, SlotHeatValue::getTotalTransitions));

        long maxTransitions = transitionMap.values().stream().mapToLong(Long::longValue).max().orElse(1L);

        List<SlotMapDto> result = slots.stream()
                .filter(s -> s.getMapPoints() != null && !s.getMapPoints().isEmpty())
                .map(slot -> {
                    long total = transitionMap.getOrDefault(slot.getName(), 0L);
                    double intensity = maxTransitions > 0 ? (double) total / maxTransitions : 0.0;
                    return SlotMapDto.builder()
                            .name(slot.getName())
                            .mapPoints(slot.getMapPoints())
                            .totalTransitions(total)
                            .heatIntensity(intensity)
                            .build();
                })
                .toList();

        return ResponseEntity.ok(result);
    }


    @PostMapping("/{levelId}/upload-map")
    public ResponseEntity<?> uploadFloorMap(@PathVariable String levelId, @RequestParam("file") MultipartFile file) {
        try {
            GroundLevel level = groundLevelService.get(levelId).orElse(null);
            if (level == null) {
                return ResponseEntity.notFound().build();
            }

            // 1. Upload file to Cloudinary
            String cdnUrl = cloudStorageService.uploadImage(file, "parking_maps");

            // 2. Update entity property with the permanent public web URL
            level.setMapImageUrl(cdnUrl);
            groundLevelService.save(level);

            return ResponseEntity.ok(Map.of("url", cdnUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}
