package com.bookingapp.cabin.backend.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor

public class BookingSummaryDTO {
    private String bookingCode;
    private String cabinName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
