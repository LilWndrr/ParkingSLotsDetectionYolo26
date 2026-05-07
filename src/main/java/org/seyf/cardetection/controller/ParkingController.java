package org.seyf.cardetection.controller;


import lombok.AllArgsConstructor;
import org.seyf.cardetection.dto.ParkingResponseDto;
import org.seyf.cardetection.repository.SlotRepository;
import org.seyf.cardetection.service.ParkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/maps")
@AllArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;
    private final SlotRepository slotRepository;

    @GetMapping("/getAll")
    public ResponseEntity<List<ParkingResponseDto>> getAll() {
        // Get occupancy data in a single query
        Map<String, SlotRepository.ParkingOccupancy> occupancyMap =
                slotRepository.getOccupancyPerParking().stream()
                        .collect(Collectors.toMap(
                                SlotRepository.ParkingOccupancy::getParkingId,
                                o -> o
                        ));

        List<ParkingResponseDto> result = parkingService.getAllParkings().stream()
                .map(p -> {
                    ParkingResponseDto dto = ParkingResponseDto.toDto(p);
                    SlotRepository.ParkingOccupancy occ = occupancyMap.get(p.getId());
                    if (occ != null) {
                        dto.applyOccupancy(occ.getTotalSlots(), occ.getOccupiedSlots());
                    }
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(result);
    }
}
