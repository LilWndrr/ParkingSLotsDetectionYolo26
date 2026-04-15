package org.seyf.cardetection.controller;


import lombok.AllArgsConstructor;
import org.seyf.cardetection.model.Parking;
import org.seyf.cardetection.service.ParkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/maps")
@AllArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @GetMapping("/getAll")
    public ResponseEntity<List<String>> getAll(){
        return ResponseEntity.ok(parkingService.getAllParkings().stream().map(Parking::getName).toList());
    }

}
