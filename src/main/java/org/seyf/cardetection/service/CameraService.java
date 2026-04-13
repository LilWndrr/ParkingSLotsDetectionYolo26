package org.seyf.cardetection.service;

import lombok.AllArgsConstructor;
import org.seyf.cardetection.model.Camera;
import org.seyf.cardetection.repository.CameraRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CameraService {

    private final CameraRepository cameraRepository;

    public boolean save(Camera camera) {
        try {
            cameraRepository.save(camera);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public Optional<Camera> getCamera(String id) {
        return cameraRepository.findById(id);
    }
}
