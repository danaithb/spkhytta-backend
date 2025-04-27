package com.bookingapp.cabin.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Setter
public class AdminBookingRequestDTO {
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
    private Integer queuePosition;
    private String guestName;
}

