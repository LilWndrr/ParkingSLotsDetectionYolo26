package org.seyf.cardetection.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camera {
    @Id
    private String id;

    @OneToMany(mappedBy = "camera")
    @JsonManagedReference
    @Builder.Default
    private List<Slot> slots = new ArrayList<>();

}
