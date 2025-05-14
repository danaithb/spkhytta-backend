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


@Service
@RequiredArgsConstructor
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

        //Kartlegger hvor mange bookinger som gjelder for hver dato
        Map<LocalDate, Integer> bookingCounts = new HashMap<>();

        for (Booking booking : bookings) {
            //Teller start og slutt separat
            bookingCounts.put(booking.getStartDate(), bookingCounts.getOrDefault(booking.getStartDate(), 0) + 1);
            bookingCounts.put(booking.getEndDate(), bookingCounts.getOrDefault(booking.getEndDate(), 0) + 1);

            //Alle datoer mellom start+1 og end-1 er fullbooket
            LocalDate date = booking.getStartDate().plusDays(1);
            while (date.isBefore(booking.getEndDate())) {
                bookingCounts.put(date, 2); // 2 betyr fullbooket
                date = date.plusDays(1);
            }
        }

        List<DayAvailabilityDTO> availabilityList = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int count = bookingCounts.getOrDefault(current, 0);
            String status;
            if (count >= 2) {
                status = "booked";
            } else if (count == 1) {
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
