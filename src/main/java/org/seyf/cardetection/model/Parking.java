package org.seyf.cardetection.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Parking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;


    @OneToMany(mappedBy = "parking")
    @ToString.Exclude
    private List<GroundLevel> groundLevels;
}
