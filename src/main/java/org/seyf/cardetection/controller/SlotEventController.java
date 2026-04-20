package org.seyf.cardetection.controller;

import lombok.RequiredArgsConstructor;
import org.seyf.cardetection.dto.SlotTransition;
import org.seyf.cardetection.service.SlotEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class SlotEventController {

    private final SlotEventService slotEventService;

    @GetMapping("/countByHours")
    public ResponseEntity<?> getAndCountSlotEventsByHors(@RequestParam("level_id") String level_id ){
        List<SlotTransition> list = slotEventService.countByHour(level_id);

        if(list.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(list);
    }
}
