package org.seyf.cardetection;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarDetectionApplication {
    static {
        OpenCV.loadLocally();
       // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) {
        SpringApplication.run(CarDetectionApplication.class, args);
        System.out.println("Open CV:"+Core.NATIVE_LIBRARY_NAME);

    }

}
