package org.seyf.cardetection.repository;

import org.seyf.cardetection.model.GroundLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroundLevelRepository extends JpaRepository<GroundLevel,String> {

    Optional<GroundLevel> findByName(String name);
    Optional<GroundLevel> findByParking_IdAndName(String parkingId, String name);
}
