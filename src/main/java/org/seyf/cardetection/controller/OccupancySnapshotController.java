package org.seyf.cardetection.controller;

import lombok.RequiredArgsConstructor;
import org.seyf.cardetection.dto.DateRequestDto;
import org.seyf.cardetection.dto.HourlyOccupancy;
import org.seyf.cardetection.dto.OccupancyFlatData;
import org.seyf.cardetection.dto.SnapshotResponseDto;
import org.seyf.cardetection.model.OccupancySnapshot;
import org.seyf.cardetection.service.OccupancySnapshotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/occupancy")
@RequiredArgsConstructor
public class OccupancySnapshotController {

    private final OccupancySnapshotService snapshotService;

    @GetMapping("/byHour")
    public ResponseEntity<?> getHourlyOccupancy(@RequestParam("ground_level_id") String levelId){

        List<HourlyOccupancy> hourlyOccupancies = snapshotService.getHourlyOccupancy(levelId);
        if(hourlyOccupancies.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(hourlyOccupancies);

    }
    @GetMapping("/byHourAndDayOfWeek")
    public ResponseEntity<?> getHourlyOccupancyByDayOfWeek(@RequestParam("ground_level_id") String levelId){

        List<OccupancyFlatData> hourlyOccupancies = snapshotService.getHourlyOccupancyByDayOfWeek(levelId);
        if(hourlyOccupancies.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(hourlyOccupancies);

    }


    @PostMapping("/byTimeInterval")
    public ResponseEntity<?> getByTimeInterval(@RequestParam("ground_level_id")String groundLevelId, @RequestBody DateRequestDto dateRequestDto){
        List<OccupancySnapshot> snapshots = snapshotService.getByTimeInterval(groundLevelId,dateRequestDto);
        if(snapshots.isEmpty()){
             return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(snapshots.stream().map(SnapshotResponseDto::toDto).toList());

    }

}
