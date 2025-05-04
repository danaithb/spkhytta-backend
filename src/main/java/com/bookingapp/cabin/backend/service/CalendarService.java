package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.dtos.CalendarRequestDTO;
import com.bookingapp.cabin.backend.dtos.DayAvailabilityDTO;
import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

//Denne er clean
@RequiredArgsConstructor
@Service
public class CalendarService {

    private final BookingRepository bookingRepository;

    public List<DayAvailabilityDTO> getAvailabilityForMonth( CalendarRequestDTO request ) {
        YearMonth yearMonth = YearMonth.parse(request.getMonth());
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        Long cabinId = request.getCabinId();

        List<Booking> bookings = bookingRepository.findByCabin_CabinIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                cabinId, "confirmed", endDate, startDate
        );
        Set<LocalDate> edgeDates = new HashSet<>();
        Set<LocalDate> fullyBookedDates = new HashSet<>();

        for (Booking booking : bookings) {
            edgeDates.add(booking.getStartDate());
            edgeDates.add(booking.getEndDate());

            LocalDate date = booking.getStartDate().plusDays(1);
            while (date.isBefore(booking.getEndDate())) {
                fullyBookedDates.add(date);
                date = date.plusDays(1);
            }
        }


        List<DayAvailabilityDTO> availabilityList = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String status;
            if (fullyBookedDates.contains(current)) {
                status = "booked";
            } else if (edgeDates.contains(current)) {
                status = "edge-booked";
            } else {
                status = "available";
            }
            availabilityList.add(new DayAvailabilityDTO(current, status));
            current = current.plusDays(1);
        }

        return availabilityList;
    }
}
