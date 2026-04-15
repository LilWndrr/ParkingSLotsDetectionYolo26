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
public class MapRequestDto {

    private String mapImageUrl;
    private List<SlotFrontendRequestDto> slots ;


}
