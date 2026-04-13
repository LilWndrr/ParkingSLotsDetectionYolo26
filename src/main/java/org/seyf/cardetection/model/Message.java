package org.seyf.cardetection.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {

    private String slotId;
    private Boolean isEmpty;
}
