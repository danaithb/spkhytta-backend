package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.BookingLog;
import com.bookingapp.cabin.backend.repository.BookingLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

//denne er clean
@RequiredArgsConstructor
@Service
public class BookingLogService {

    private final BookingLogRepository bookingLogRepository;

    @Transactional
    public void recordBookingLog(Booking booking, String action, String performedBy) {
        BookingLog log = new BookingLog();
        log.setBooking(booking);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setTimestamp(LocalDateTime.now());
        bookingLogRepository.save(log);
    }
}
