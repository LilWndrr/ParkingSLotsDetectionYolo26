package org.seyf.cardetection.repository;

import org.seyf.cardetection.model.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraRepository extends JpaRepository<Camera, String> {

}
