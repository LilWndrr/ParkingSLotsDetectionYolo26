package org.seyf.cardetection.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "slot", uniqueConstraints = {
        // This replaces your compound primary key!
        // Prevents duplicate slots like "A1" on the same floor.
        @UniqueConstraint(columnNames = {"level_id", "name"})
})
public class Slot {

    @Id
    @GeneratedValue(strategy =GenerationType.UUID)
    private String id;

    private String name;

    private int original_width;
    private int original_height;
    private boolean isEmpty;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<List<Double>> points;

    @Column(name = "map_points")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<List<Double>> mapPoints;

    @ManyToOne
    @JoinColumn(name = "camera_id")
    @JsonBackReference
    private Camera camera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private GroundLevel level;



}
