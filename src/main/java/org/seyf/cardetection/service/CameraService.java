package org.seyf.cardetection.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seyf.cardetection.model.Camera;
import org.seyf.cardetection.repository.CameraRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class CameraService {

    private final CameraRepository cameraRepository;

    public Camera save(Camera camera) {

        return     cameraRepository.save(camera);

    }

    public Optional<Camera> getCamera(String id) {
        return cameraRepository.findById(id);
    }

    public Optional<Camera> getByName(String name){
        List<Camera> results = cameraRepository.findByName(name);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            log.warn("Found {} duplicate camera entries with name '{}'. Using the first one. " +
                     "Please clean up duplicate rows in the database.", results.size(), name);
        }
        return Optional.of(results.get(0));
    }
}
