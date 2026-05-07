package org.seyf.cardetection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.seyf.cardetection.model.Parking;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ParkingResponseDto {

    private String id;
    private String name;
    private double longitude;
    private double latitude;
    private long totalSlots;
    private long occupiedSlots;
    private long availableSlots;
    private double occupancyRate;

    public static ParkingResponseDto toDto(Parking parking) {
        return ParkingResponseDto.builder()
                .id(parking.getId())
                .name(parking.getName())
                .longitude(parking.getLongitude() != null ? parking.getLongitude() : 0)
                .latitude(parking.getLatitude() != null ? parking.getLatitude() : 0)
                .build();
    }

    public void applyOccupancy(long total, long occupied) {
        this.totalSlots = total;
        this.occupiedSlots = occupied;
        this.availableSlots = total - occupied;
        this.occupancyRate = total > 0 ? (occupied * 100.0 / total) : 0;
    }
}
