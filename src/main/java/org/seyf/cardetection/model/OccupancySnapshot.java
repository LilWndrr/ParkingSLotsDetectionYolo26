package org.seyf.cardetection.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Indexed;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "occupancy_snapshot",indexes = {
        @Index (columnList = "ground_level_id, recorded_at" )
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancySnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ground_level_id", nullable = false)
    private GroundLevel groundLevel;

    private int totalSlots;
    private int occupiedSlots;
    private double occupancyRate;

    @Column(name = "recorded_at",nullable = false)
    private LocalDateTime recordedAt;

    private int dayOfWeek;
    private int hourOfDay;


    private boolean isWeekend;
}
