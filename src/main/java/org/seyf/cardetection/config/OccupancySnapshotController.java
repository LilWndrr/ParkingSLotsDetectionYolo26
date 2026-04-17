package org.seyf.cardetection.config;

import lombok.RequiredArgsConstructor;
import org.seyf.cardetection.dto.DateRequestDto;
import org.seyf.cardetection.dto.HourlyOccupancy;
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
    public ResponseEntity<?> getHourlyOccupancy(){

        List<HourlyOccupancy> hourlyOccupancies = snapshotService.getHourlyOccupancy();
        if(hourlyOccupancies.isEmpty()){
            return ResponseEntity.badRequest().body("Smth Went Wrong");
        }

        return ResponseEntity.ok(hourlyOccupancies);

    }
    @PostMapping("/byTimeInterval")
    public ResponseEntity<?> getByTimeInterval(@RequestParam("ground_level_id")String groundLevelId, @RequestBody DateRequestDto dateRequestDto){
        List<OccupancySnapshot> snapshots = snapshotService.getByTimeInterval(groundLevelId,dateRequestDto);
        if(snapshots.isEmpty()){
            return ResponseEntity.badRequest().body("There is no records at this time interval");
        }

        return ResponseEntity.ok(snapshots.stream().map(SnapshotResponseDto::toDto).toList());

    }

}
