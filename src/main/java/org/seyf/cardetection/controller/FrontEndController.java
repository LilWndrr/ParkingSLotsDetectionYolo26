package org.seyf.cardetection.controller;

import lombok.AllArgsConstructor;
import org.seyf.cardetection.dto.MapRequestDto;
import org.seyf.cardetection.dto.SlotFrontendRequestDto;
import org.seyf.cardetection.model.Camera;
import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.model.Parking;
import org.seyf.cardetection.model.Slot;
import org.seyf.cardetection.service.GroundLevelService;
import org.seyf.cardetection.service.OccupancySnapshotService;
import org.seyf.cardetection.service.ParkingService;
import org.seyf.cardetection.service.SlotService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/display")
public class FrontEndController {


    private final SlotService slotService;
    private final GroundLevelService groundLevelService;
    private final ParkingService parkingService;
    private final OccupancySnapshotService snapshotService;
    private final RedisTemplate<String,Object> redisTemplate;

    @GetMapping("/map")
    public ResponseEntity<?> getParkingMap(@RequestParam("parking_name") String parkingName,
                                           @RequestParam("ground_level_id") String levelName) {

        Parking parking = parkingService.getByName(parkingName).orElse(null);
        if (parking == null) {
            return ResponseEntity.badRequest().body("No such parking with this name");
        }

        GroundLevel groundLevel = groundLevelService.getByParkingIdAndName(parking.getId(), levelName).orElse(null);
        if (groundLevel == null) {
            return ResponseEntity.badRequest().body("No such ground level with this id");
        }

        Map<String, Boolean> liveSlotsStates = new HashMap<>();
        for (Camera cam : groundLevel.getCameras()){
            String redisKey= "parking:" +parking.getName() + ":ground_level:" + groundLevel.getName() + ":camera:"+ cam.getName() +":slots";

            Map<Object,Object> liveStandings =  redisTemplate.opsForHash().entries(redisKey);

            liveStandings.forEach((slotName,isEmpty)->{
                liveSlotsStates.put(slotName.toString(),(Boolean) isEmpty);
            });

        }

        List<SlotFrontendRequestDto> slotsDto = slotService.getByGrounfLevel(groundLevel).stream().map(slot -> {
            boolean currentLiveState = liveSlotsStates.getOrDefault(slot.getName(),true);

            return SlotFrontendRequestDto.builder()
                    .name(slot.getName())
                    .isEmpty(currentLiveState)
                    .mapPoints(slot.getMapPoints())
                    .build();
        }).toList();

        MapRequestDto mapRequestDto = MapRequestDto.builder()
                .slots(slotsDto)
                .occupancyRate(snapshotService.getOccupancyRateByGroundLevel(groundLevel.getId()))
                .mapImageUrl(groundLevel.getMapImageUrl())
                .build();

        return ResponseEntity.ok(mapRequestDto);
    }

}
