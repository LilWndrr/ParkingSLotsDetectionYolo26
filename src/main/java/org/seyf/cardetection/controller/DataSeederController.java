package org.seyf.cardetection.controller;

import lombok.RequiredArgsConstructor;
import org.seyf.cardetection.service.DataSeederService;
import org.seyf.cardetection.service.GroundLevelService;
import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.repository.OccupancySnapshotRepository;
import org.seyf.cardetection.repository.SlotEventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seed")
@RequiredArgsConstructor
public class DataSeederController {

    private final DataSeederService seederService;
    private final GroundLevelService groundLevelService;
    private final OccupancySnapshotRepository snapshotRepository;
    private final SlotEventRepository slotEventRepository;

    /**
     * Seeds the database with realistic fake occupancy data.
     * Uses existing slots from the database.
     *
     * Example: POST /api/v1/seed?ground_level_id=abc-123&weeks=6
     */
    @PostMapping
    public ResponseEntity<String> seed(
            @RequestParam("ground_level_id") String groundLevelId,
            @RequestParam(value = "weeks", defaultValue = "6") int weeks) {

        if (weeks < 1 || weeks > 52) {
            return ResponseEntity.badRequest().body("Weeks must be between 1 and 52");
        }

        String result = seederService.seedData(groundLevelId, weeks);
        return ResponseEntity.ok(result);
    }

    /**
     * Deletes all seeded snapshots and events for a ground level.
     *
     * Example: DELETE /api/v1/seed?ground_level_id=abc-123
     */
    @DeleteMapping
    @Transactional
    public ResponseEntity<String> clear(@RequestParam("ground_level_id") String groundLevelId) {
        GroundLevel level = groundLevelService.get(groundLevelId).orElse(null);
        if (level == null) {
            return ResponseEntity.badRequest().body("Ground level not found");
        }

        slotEventRepository.deleteByGroundLevel(level);
        snapshotRepository.deleteByGroundLevel(level);

        return ResponseEntity.ok("Cleared all snapshots and events for level: " + groundLevelId);
    }
}
