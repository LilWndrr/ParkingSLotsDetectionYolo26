package org.seyf.cardetection.repository;


import org.seyf.cardetection.model.Camera;
import org.seyf.cardetection.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, String> {

    public List<Slot> findSlotByCamera(Camera camera);


}
