package org.seyf.cardetection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class SlotFrontendRequestDto {

    private String name;
    private boolean isEmpty;
    private List<List<Double>>  mapPoints;
}
