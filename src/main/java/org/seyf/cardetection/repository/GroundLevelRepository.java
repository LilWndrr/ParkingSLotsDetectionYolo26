package org.seyf.cardetection.repository;

import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.model.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroundLevelRepository extends JpaRepository<GroundLevel,String> {

    Optional<GroundLevel> findByName(String name);

    List<GroundLevel> findByParking_Id(String parkingId);
    Optional<GroundLevel> findByParking_IdAndName(String parkingId, String name);
}
