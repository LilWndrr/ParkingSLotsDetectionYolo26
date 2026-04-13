package org.seyf.cardetection.controller;


import lombok.AllArgsConstructor;
import org.seyf.cardetection.service.IngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ingestion")
@AllArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;


    @PostMapping("/forward")
    public ResponseEntity<String> forward (@RequestParam("file") MultipartFile file,
                                           @RequestParam("camera_id") String cameraId,
                                           @RequestParam("timestamp") Long timestamp){
          if(ingestionService.sendToMessageBroker(file,cameraId,timestamp))  {
                return  ResponseEntity.ok().body("File forwarded successfully");
          }

          return ResponseEntity.badRequest().body("File was not forwarded");
    }

}
