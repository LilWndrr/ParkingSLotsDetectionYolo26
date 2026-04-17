package org.seyf.cardetection.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DateRequestDto {

        private LocalDate from;
        private LocalDate to;
}
