package org.seyf.cardetection.model;


import jakarta.persistence.*;
import lombok.*;


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
    @ToString.Exclude
    private List<Camera> cameras;

    @OneToMany(mappedBy = "level") // OPTIONAL: Good for fetching all slots on a level
    @ToString.Exclude
    private List<Slot> slots;

    @Column(name = "map_image_url")
    private String mapImageUrl;

}
