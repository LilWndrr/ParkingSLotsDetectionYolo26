package org.seyf.cardetection.repository;


import org.seyf.cardetection.model.Camera;
import org.seyf.cardetection.model.GroundLevel;
import org.seyf.cardetection.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, String> {

    List<Slot> findSlotByCamera(Camera camera);

    Optional<Slot> getByNameAndLevel(String name, GroundLevel level);

    List<Slot> findAllByLevel(GroundLevel level);

    interface ParkingOccupancy {
        String getParkingId();
        long getTotalSlots();
        long getOccupiedSlots();
    }

    @Query("SELECT gl.parking.id AS parkingId, " +
           "COUNT(s) AS totalSlots, " +
           "SUM(CASE WHEN s.isEmpty = false THEN 1 ELSE 0 END) AS occupiedSlots " +
           "FROM Slot s JOIN s.level gl GROUP BY gl.parking.id")
    List<ParkingOccupancy> getOccupancyPerParking();
}
