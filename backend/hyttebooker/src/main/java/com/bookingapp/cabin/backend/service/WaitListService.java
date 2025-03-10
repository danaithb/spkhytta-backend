package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WaitListService {

    @Autowired
    private BookingRepository bookingRepository;

    public void promoteFromWaitlist(Long cabinId) {
        List<Booking> waitlistBookings = bookingRepository.findByCabin_CabinIdAndStatusOrderByQueuePositionAsc(
                cabinId, "waitlist"
        );

        if (waitlistBookings.isEmpty()) {
            System.out.println("Ingen venteliste-kandidater for hytte " + cabinId);
            return;
        }

        Booking nextInLine = waitlistBookings.get(0);
        nextInLine.setStatus("confirmed");
        nextInLine.setQueuePosition(null);
        bookingRepository.save(nextInLine);

        System.out.println("Booking ID " + nextInLine.getBookingId() + " er nå bekreftet fra ventelisten!");

        for (int i = 1; i < waitlistBookings.size(); i++) {
            Booking booking = waitlistBookings.get(i);
            booking.setQueuePosition(i);
            bookingRepository.save(booking);
        }
    }
}
