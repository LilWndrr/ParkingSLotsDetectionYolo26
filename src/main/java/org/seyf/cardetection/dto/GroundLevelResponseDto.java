package org.seyf.cardetection.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.seyf.cardetection.model.GroundLevel;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class GroundLevelResponseDto {

    private String id;
    private String name;
    private String imageUrl;


    public static GroundLevelResponseDto toDto (GroundLevel level){
        if(level==null){
            return new GroundLevelResponseDto();
        }
        return GroundLevelResponseDto.builder().id(level.getId())
                .name(level.getName())
                .imageUrl(level.getMapImageUrl()).build();
    }
}



