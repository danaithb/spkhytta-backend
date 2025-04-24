package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.dtos.DayAvailabilityDTO;
import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CalendarService {

    private final BookingRepository bookingRepository;

    @Autowired
    public CalendarService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Henter tilgjengelighet for en bestemt dato-periode for en hytte
    public List<DayAvailabilityDTO> getAvailabilityForDates(LocalDate startDate, LocalDate endDate, Long cabinId) {
        // Henter bekreftede bookinger som overlapper med perioden
        List<Booking> bookings = bookingRepository.findByCabin_CabinIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                cabinId, "confirmed", endDate, startDate
        );

        // Samler opp alle datoer som er opptatt
        Set<LocalDate> bookedDates = new HashSet<>();
        for (Booking booking : bookings) {
            LocalDate date = booking.getStartDate();
            while (!date.isAfter(booking.getEndDate().minusDays(1))) {
                bookedDates.add(date);
                date = date.plusDays(1);
            }
        }

        // Lager oversikt over hver dag i perioden og status
        List<DayAvailabilityDTO> availabilityList = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String status = bookedDates.contains(current) ? "booked" : "available";
            availabilityList.add(new DayAvailabilityDTO(current, status));
            current = current.plusDays(1);
        }

        return availabilityList;
    }
}
