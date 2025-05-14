package com.bookingapp.cabin.backend.dtos;

import lombok.Data;

import java.time.LocalDate;
@Data
public class LotteryDatesRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
