package org.seyf.cardetection.service;

import lombok.RequiredArgsConstructor;
import org.seyf.cardetection.model.SlotEvent;
import org.seyf.cardetection.repository.SlotEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotEventService {

    private final SlotEventRepository eventRepository;

    public void save (SlotEvent event){
        eventRepository.save(event);
    }

    public void saveAll (List<SlotEvent> events){
        eventRepository.saveAll(events);
    }
}
