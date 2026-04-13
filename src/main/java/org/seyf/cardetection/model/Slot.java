package org.seyf.cardetection.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Slot {

    @Id
    private String id;

    private int original_width;
    private int original_height;
    private boolean isEmpty;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<List<Double>> points;

    @ManyToOne
    @JoinColumn(name = "camera_id")
    @JsonBackReference
    private Camera camera;

}
