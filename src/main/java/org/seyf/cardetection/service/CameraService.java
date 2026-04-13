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

    public Camera save(Camera camera) {

        return     cameraRepository.save(camera);

    }

    public Optional<Camera> getCamera(String id) {
        return cameraRepository.findById(id);
    }

    public Optional<Camera> getByName(String name){
        return cameraRepository.findByName(name);
    }
}
