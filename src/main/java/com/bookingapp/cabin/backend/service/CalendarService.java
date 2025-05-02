package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.dtos.DayAvailabilityDTO;
import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class CalendarService {

    private final BookingRepository bookingRepository;

    @Autowired
    public CalendarService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Henter tilgjengelighet for en bestemt dato-periode for en hytte
    public List<DayAvailabilityDTO> getAvailabilityForDates(LocalDate startDate, LocalDate endDate, Long cabinId) {
        List<Booking> bookings = bookingRepository.findByCabin_CabinIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                cabinId, "confirmed", endDate, startDate
        );

        Map<LocalDate, Integer> bookingCounts = new HashMap<>();

        for (Booking booking : bookings) {
            // Tell start- og sluttdatoer separat
            LocalDate[] edgeDates = { booking.getStartDate(), booking.getEndDate() };
            for (LocalDate d : edgeDates) {
                bookingCounts.put(d, bookingCounts.getOrDefault(d, 0) + 1);
            }

            // Alle datoer mellom start+1 og end-1 = fullbooket
            LocalDate date = booking.getStartDate().plusDays(1);
            while (date.isBefore(booking.getEndDate())) {
                bookingCounts.put(date, 2); // Marker som 100 % opptatt
                date = date.plusDays(1);
            }
        }

        List<DayAvailabilityDTO> availabilityList = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int count = bookingCounts.getOrDefault(current, 0);
            String status = count >= 2 ? "booked" : "available";
            availabilityList.add(new DayAvailabilityDTO(current, status));
            current = current.plusDays(1);
        }

        return availabilityList;
    }
}
