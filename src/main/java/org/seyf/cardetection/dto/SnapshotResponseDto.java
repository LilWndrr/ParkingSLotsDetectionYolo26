package org.seyf.cardetection.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.seyf.cardetection.model.OccupancySnapshot;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SnapshotResponseDto {
    private String id;
    private double occupancyRate;
    private int occupiedSlots;
    private int totalSlots;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recordedAt;


    public static SnapshotResponseDto toDto (OccupancySnapshot snapshot){

        return SnapshotResponseDto.builder()
                .id(snapshot.getId())
                .occupancyRate(snapshot.getOccupancyRate())
                .recordedAt(snapshot.getRecordedAt())
                .totalSlots(snapshot.getTotalSlots())
                .occupiedSlots(snapshot.getOccupiedSlots())
                .build();
    }
}