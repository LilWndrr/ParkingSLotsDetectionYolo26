package org.seyf.cardetection;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarDetectionApplication {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) {
        SpringApplication.run(CarDetectionApplication.class, args);
        System.out.println(Core.NATIVE_LIBRARY_NAME);

    }

}
