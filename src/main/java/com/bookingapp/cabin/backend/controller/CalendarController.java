package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.dtos.CalendarRequestDTO;
import com.bookingapp.cabin.backend.dtos.DayAvailabilityDTO;
import com.bookingapp.cabin.backend.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
//Returnerer tilgjengelige og opptatte dager for valgt måned og hytte
public class CalendarController {

    private final CalendarService calendarService;

    @CrossOrigin(origins = "https://spkhytta.web.app")
    @PostMapping("/availability")
    public ResponseEntity<List<DayAvailabilityDTO>> getAvailabilityForMonth(
            @RequestBody CalendarRequestDTO calendarRequest
    ) {
        List<DayAvailabilityDTO> availability = calendarService.getAvailabilityForMonth(calendarRequest);
        return ResponseEntity.ok(availability);

        }

    }

