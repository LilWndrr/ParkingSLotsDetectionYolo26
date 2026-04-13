package org.seyf.cardetection.repository;


import org.seyf.cardetection.model.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParkingRepository extends JpaRepository<Parking,String> {
    public Optional<Parking> findByName(String name);
}
