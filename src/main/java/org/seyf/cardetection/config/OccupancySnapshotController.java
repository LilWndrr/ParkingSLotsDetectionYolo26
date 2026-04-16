package org.seyf.cardetection.config;

import lombok.RequiredArgsConstructor;
import org.seyf.cardetection.service.OccupancySnapshotService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/occupancy")
@RequiredArgsConstructor
public class OccupancySnapshotController {

    private final OccupancySnapshotService snapshotService;



}
