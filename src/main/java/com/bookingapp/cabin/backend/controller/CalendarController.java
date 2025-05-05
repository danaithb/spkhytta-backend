package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.dtos.CalendarRequestDTO;
import com.bookingapp.cabin.backend.dtos.DayAvailabilityDTO;
import com.bookingapp.cabin.backend.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

//denne er clean
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    //@CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/availability")
    public ResponseEntity<List<DayAvailabilityDTO>> getAvailabilityForMonth(
            @RequestBody CalendarRequestDTO calendarRequest
    ) {
        List<DayAvailabilityDTO> availability = calendarService.getAvailabilityForMonth(calendarRequest);
        return ResponseEntity.ok(availability);

        }

    }

