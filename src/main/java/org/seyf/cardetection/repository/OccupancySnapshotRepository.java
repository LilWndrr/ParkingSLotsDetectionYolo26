package org.seyf.cardetection.repository;

import org.seyf.cardetection.dto.HourlyOccupancy;
import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.model.OccupancySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OccupancySnapshotRepository extends JpaRepository<OccupancySnapshot,String> {

    Optional<OccupancySnapshot> findFirstByGroundLevelOrderByRecordedAtDesc(GroundLevel groundLevel);
    List<OccupancySnapshot>  findByGroundLevelAndRecordedAtBetween(GroundLevel groundLevel, LocalDateTime from, LocalDateTime to);
    @Query("select s.hourOfDay as hourOfDay, avg(s.occupancyRate) as averageRate from OccupancySnapshot s group by s.hourOfDay order by s.hourOfDay ASC")
    List<HourlyOccupancy> findAverageOccupancyByHour();




}
