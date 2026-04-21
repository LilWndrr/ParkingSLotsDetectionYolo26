package org.seyf.cardetection.controller;

import lombok.RequiredArgsConstructor;
import org.seyf.cardetection.dto.SlotRequestDto;
import org.seyf.cardetection.model.Slot;
import org.seyf.cardetection.service.SlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v2/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/all")
    public ResponseEntity<?> getAll(){

        List<SlotRequestDto> list = slotService.getAll().stream().map(SlotRequestDto::toDto).toList();
        if(list.isEmpty()){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(list);
    }
}
