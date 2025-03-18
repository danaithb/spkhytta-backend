package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//denne klassen er fikset
@Service
public class WaitListService {
    private static final Logger logger = LoggerFactory.getLogger(WaitListService.class);
    private BookingRepository bookingRepository;

    @Autowired
    public WaitListService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    //oppdaterer ventelisten etter loddtrekning
    public void promoteFromWaitlist(Long cabinId) {
        List<Booking> waitlistBookings = bookingRepository.findByCabin_CabinIdAndStatusOrderByQueuePositionAsc(
                cabinId, "waitlist"
        );

        if (waitlistBookings.isEmpty()) {
            logger.info("Ingen venteliste-kandidater for hytte {}", cabinId);
            return;
        }

        Booking nextInLine = waitlistBookings.get(0);
        nextInLine.setStatus("confirmed");
        nextInLine.setQueuePosition(null);
        bookingRepository.save(nextInLine);
        logger.info("Booking ID {} er nå bekreftet fra ventelisten!", nextInLine.getBookingId());

        for (int i = 1; i < waitlistBookings.size(); i++) {
            Booking booking = waitlistBookings.get(i);
            booking.setQueuePosition(i);
            bookingRepository.save(booking);
        }
        logger.info("Ventelisten er oppdatert for hytte {}", cabinId);
    }
}
