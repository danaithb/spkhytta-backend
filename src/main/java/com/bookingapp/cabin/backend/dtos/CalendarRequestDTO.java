package com.bookingapp.cabin.backend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarRequestDTO {
    private String month;
    private Long cabinId;
}
