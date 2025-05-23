package com.bookingapp.cabin.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
//DTO brukt av admin for å opprette en booking på vegne av en bruker

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AdminCreateBookingDTO {
    private Long userId;
    private Long cabinId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfGuests;
    private Boolean businessTrip;
}
