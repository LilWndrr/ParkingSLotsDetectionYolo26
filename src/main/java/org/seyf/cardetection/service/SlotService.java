package org.seyf.cardetection.service;

import lombok.AllArgsConstructor;
import org.seyf.cardetection.model.Camera;
import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.model.Slot;
import org.seyf.cardetection.repository.CameraRepository;
import org.seyf.cardetection.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;


    public boolean addSlot(Slot slot) {
        try {
            slotRepository.save(slot);
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void update(Slot slot) {
        slotRepository.save(slot);
    }

    public List<Slot> getAll(){
        return slotRepository.findAll();
    }

    public Optional<List<Slot>> getByCamera(Camera camera) {
        var slots = slotRepository.findSlotByCamera(camera);
        return Optional.of(slots);
    }


    public Optional<Slot> getByNameAndLevel(String slotName, GroundLevel level) {

        return slotRepository.getByNameAndLevel(slotName, level);
    }

    public List<Slot> getByGrounfLevel(GroundLevel groundLevel) {
        return slotRepository.findAllByLevel(groundLevel);
    }

    public void saveAll(List<Slot> stateChangedSlots) {
        slotRepository.saveAll(stateChangedSlots);

    }
}
