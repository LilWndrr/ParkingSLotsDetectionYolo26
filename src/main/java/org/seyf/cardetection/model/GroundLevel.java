package org.seyf.cardetection.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ground_level", uniqueConstraints = {

        @UniqueConstraint(columnNames = {"parking_id", "name"})
})
public class GroundLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id",nullable = false)
    private Parking parking;


    @OneToMany(mappedBy = "level")
    private List<Camera> cameras;

    @OneToMany(mappedBy = "level") // OPTIONAL: Good for fetching all slots on a level
    private List<Slot> slots;

}
