package org.seyf.cardetection.repository;

import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.model.OccupancySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OccupancySnapshotRepository extends JpaRepository<OccupancySnapshot,String> {

    Optional<OccupancySnapshot> findFirstByGroundLevelOrderByRecordedAtDesc(GroundLevel groundLevel);

}
