package org.seyf.cardetection.service;


import lombok.AllArgsConstructor;
import org.seyf.cardetection.model.Parking;
import org.seyf.cardetection.repository.ParkingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ParkingService {

    private final ParkingRepository parkingRepository;


    public Optional<Parking> getByName(String name){
        return parkingRepository.findByName(name);
    }

    public Parking save (Parking parking){

         return  parkingRepository.save(parking);

    }

    public List<Parking> getAllParkings(){
        return  parkingRepository.findAll();
    }
}
