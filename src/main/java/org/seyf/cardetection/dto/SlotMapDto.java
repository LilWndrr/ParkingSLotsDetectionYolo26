package org.seyf.cardetection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlotMapDto {
    private String name;
    private List<List<Double>> mapPoints;
    private long totalTransitions;  // heat value
    private double heatIntensity;   // 0.0 - 1.0, normalized
}
