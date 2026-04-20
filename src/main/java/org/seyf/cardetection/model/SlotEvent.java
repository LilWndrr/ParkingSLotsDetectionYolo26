package org.seyf.cardetection.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "slot_event", indexes = {
        @Index(columnList = "ground_level_id,occurred_at")
})
public class SlotEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String slotName;
    private String cameraName;
    private int dayOfWeek;
    private int hourOfDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ground_level_id", nullable = false)
    private GroundLevel groundLevel;

    private String parkingName;
    private boolean isEmpty;

    @Column(name="occurred_at",nullable = false)
    private LocalDateTime occurredAt;
}
