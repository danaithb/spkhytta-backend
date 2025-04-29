package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.BookingLog;
import com.bookingapp.cabin.backend.repository.BookingLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingLogService {

    private final BookingLogRepository bookingLogRepository;

    @Autowired
    public BookingLogService(BookingLogRepository bookingLogRepository) {
        this.bookingLogRepository = bookingLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordBookingLog(Booking booking, String action, String performedBy) {
        BookingLog log = new BookingLog();
        log.setBooking(booking);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setTimestamp(LocalDateTime.now());
        bookingLogRepository.save(log);
    }
}
