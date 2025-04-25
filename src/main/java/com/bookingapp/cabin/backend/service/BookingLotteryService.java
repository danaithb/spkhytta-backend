package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import com.bookingapp.cabin.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bookingapp.cabin.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class BookingLotteryService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(BookingLotteryService.class);
    private final BookingRepository bookingRepository;

    @Autowired
    public BookingLotteryService(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

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

    // Behandler bookinger for en hytte og velger en vinner via loddtrekning
    public void processBookings(Long cabinId, LocalDate startDate, LocalDate endDate) {
        List<Booking> overlappingBookings = bookingRepository.findByCabin_CabinIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                cabinId, "pending", endDate, startDate
        );

        if (overlappingBookings.isEmpty()) {
            logger.info("Ingen pending bookinger funnet for hytte " + cabinId);
            return;
        }

        Booking selectedBooking = conductLottery(overlappingBookings);
        if (selectedBooking == null) {
            logger.info("Ingen booking ble valgt i loddtrekningen for hytte {}", cabinId);
            return;
        }

        selectedBooking.setStatus("confirmed");
        selectedBooking.setQueuePosition(null);

        if (!selectedBooking.isRestBooking()) {
            Users user = selectedBooking.getUser();
            int cost = selectedBooking.getPointsRequired();
            int before = user.getPoints();
            int after = before - cost;

            user.setPoints(after);
            userRepository.save(user);

            selectedBooking.setPointsDeducted(cost);
            selectedBooking.setPointsBefore(before);
            selectedBooking.setPointsAfter(after);
            logger.info("Trekk {} poeng fra bruker {} (ny saldo: {})",
                    cost, user.getEmail(), user.getPoints());
        } else {
            logger.info("Restbooking – poeng trekkes ikke for booking ID {}", selectedBooking.getBookingId());
        }

        bookingRepository.save(selectedBooking);
        logger.info("Booking ID {} vant loddtrekningen!", selectedBooking.getBookingId());

        int queuePosition = 1;
        for (Booking booking : overlappingBookings) {
            if (!booking.getBookingId().equals(selectedBooking.getBookingId())) {
                booking.setStatus("waitlist");
                booking.setQueuePosition(queuePosition++);
                bookingRepository.save(booking);
                logger.info("Booking ID {} er satt til venteliste med køposisjon {}", booking.getBookingId(), booking.getQueuePosition());
            }
        }
        logger.info("Ventelisten er oppdatert for hytte {}", cabinId);
    }
}

