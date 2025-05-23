package com.bookingapp.cabin.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@AllArgsConstructor
@Getter
@Setter

public class BookingRequestDTO {
    private Long cabinId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfGuests;
    private boolean businessTrip;


}