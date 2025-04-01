package com.bookingapp.cabin.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
public class AdminBookingRequestDTO {
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private double price;
    private Integer queuePosition;
}

