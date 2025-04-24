package com.bookingapp.cabin.backend.dtos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor


public class DayAvailabilityDTO {
    private LocalDate date;
    private String status;

}
