package org.seyf.cardetection.controller;


import lombok.AllArgsConstructor;
import org.seyf.cardetection.model.Parking;
import org.seyf.cardetection.service.ParkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/maps")
@AllArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @GetMapping("/getAll")
    public ResponseEntity<List<Map<String, String>>> getAll(){
        List<Map<String, String>> result = parkingService.getAllParkings().stream()
                .map(p -> Map.of("id", p.getId(), "name", p.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

}
