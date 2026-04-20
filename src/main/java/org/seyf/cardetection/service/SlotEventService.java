package org.seyf.cardetection.service;

import lombok.RequiredArgsConstructor;
import org.seyf.cardetection.dto.SlotTransition;
import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.model.SlotEvent;
import org.seyf.cardetection.repository.SlotEventRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotEventService {

    private final SlotEventRepository eventRepository;
    private final GroundLevelService levelService;

    public void save (SlotEvent event){
        eventRepository.save(event);
    }

    public void saveAll (List<SlotEvent> events){
        eventRepository.saveAll(events);
    }

    public List<SlotTransition> countByHour(String levelId){
        GroundLevel level=levelService.get(levelId).orElse(null);
        if(level==null){
            return new ArrayList<>();
        }
        return eventRepository.countSlotEventAppearanceGroupBySlotAndByHourOfDay(level);
    }
}
