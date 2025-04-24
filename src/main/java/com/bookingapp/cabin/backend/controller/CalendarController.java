package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.dtos.CalendarRequestDTO;
import com.bookingapp.cabin.backend.dtos.DayAvailabilityDTO;
import com.bookingapp.cabin.backend.service.BookingService;
import com.bookingapp.cabin.backend.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final BookingService bookingService;
    private final CalendarService calendarService;

    @Autowired
    public CalendarController(BookingService bookingService, CalendarService calendarService) {
        this.bookingService = bookingService;
        this.calendarService = calendarService;
    }

    @PostMapping("/availability")
    public ResponseEntity<List<DayAvailabilityDTO>> getAvailabilityForMonth(
            @RequestBody CalendarRequestDTO calendarRequest
    ) {
        YearMonth yearMonth = YearMonth.parse(calendarRequest.getMonth());
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<DayAvailabilityDTO> availability =
                calendarService.getAvailabilityForDates(start, end, calendarRequest.getCabinId());

        return ResponseEntity.ok(availability);
    }
}
