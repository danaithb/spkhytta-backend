package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import com.bookingapp.cabin.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Random;

//denne er clean
@RequiredArgsConstructor
@Service
public class BookingLotteryService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(BookingLotteryService.class);
    private final BookingRepository bookingRepository;

    public Booking conductLottery(List<Booking> pendingBookings) {
        if (pendingBookings.isEmpty()){
            logger.info("Ingen tilgjengelige bookinger for loddtrekning.");
            return null;
    }

        Booking selectedBooking = pendingBookings.get(new Random().nextInt(pendingBookings.size()));
        selectedBooking.setStatus("confirmed");
        bookingRepository.save(selectedBooking);

        logger.info("Booking ID {} er valgt i loddtrekning og bekreftet.", selectedBooking.getBookingId());
        return selectedBooking;
    }


}

