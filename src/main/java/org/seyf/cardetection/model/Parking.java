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
public class Parking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;


    @OneToMany(mappedBy = "parking")
    private List<GroundLevel> groundLevels;
}
