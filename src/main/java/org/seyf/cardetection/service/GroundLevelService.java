package org.seyf.cardetection.service;


import lombok.AllArgsConstructor;
import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.repository.GroundLevelRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class GroundLevelService {
    private final GroundLevelRepository groundLevelRepository;


    public Optional<GroundLevel> get(String id){
        return groundLevelRepository.findById(id);
    }

    public GroundLevel save(GroundLevel groundLevel){

       return      groundLevelRepository.save(groundLevel);


    }

    public Optional<GroundLevel> getByName(String name) {
        return groundLevelRepository.findByName(name);
    }

    public Optional<GroundLevel> getByParkingIdAndName(String parkingId, String name){
        return groundLevelRepository.findByParking_IdAndName(parkingId, name);
    }

    public List<GroundLevel> getAll(){
        return groundLevelRepository.findAll();
    }
}
