package org.seyf.cardetection.repository;

import org.seyf.cardetection.model.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CameraRepository extends JpaRepository<Camera, String> {

    List<Camera> findByName(String name);
}
