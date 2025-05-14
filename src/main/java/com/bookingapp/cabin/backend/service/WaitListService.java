package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RequiredArgsConstructor
@Service

//oppdaterer venteliste etter kansellering eller loddtrekning
public class WaitListService {
    private static final Logger logger = LoggerFactory.getLogger(WaitListService.class);
    private BookingRepository bookingRepository;

    //Bekrefter neste booking i ventelisten og oppdaterer rekkefølgen
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
