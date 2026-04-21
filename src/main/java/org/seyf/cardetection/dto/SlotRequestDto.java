package org.seyf.cardetection.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.seyf.cardetection.model.Slot;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class SlotRequestDto {

    private String id;

    private String name;

    private int original_width;
    private int original_height;
    private boolean isEmpty;

    private List<List<Double>> points;
    private List<List<Double>> mapPoints;


    public static SlotRequestDto toDto(Slot slot){
        return SlotRequestDto.builder().id(slot.getId())
                .mapPoints(slot.getMapPoints())
                .isEmpty(slot.isEmpty())
                .name(slot.getName())
                .original_height(slot.getOriginal_height())
                .original_width(slot.getOriginal_width())
                .points(slot.getPoints()).build();
    }
}
