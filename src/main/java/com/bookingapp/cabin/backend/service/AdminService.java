package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.AdminRepository;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import com.bookingapp.cabin.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingLotteryService bookingLotteryService;


    @Autowired
    public AdminService(AdminRepository adminRepository, UserRepository userRepository, BookingRepository bookingRepository, BookingLotteryService bookingLotteryService) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.bookingLotteryService = bookingLotteryService;
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<Booking> getAllBookings() {
        return adminRepository.findAll();
    }

    public Booking getBookingById(Long bookingId) {
        return adminRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking ikke funnet"));
    }


    public void deleteBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        adminRepository.delete(booking);
    }

    public Booking editBooking(Long bookingId, String guestName, LocalDate startDate, LocalDate endDate, String status, BigDecimal price) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking ikke funnet"));

        if (guestName != null && !guestName.isEmpty()) {
            Users user = booking.getUser();
            user.setName(guestName);
            userRepository.save(user);
        }

        if (startDate != null) {
            booking.setStartDate(startDate);
        }

        if (endDate != null) {
            booking.setEndDate(endDate);
        }

        if (status != null && !status.isEmpty()) {
            booking.setStatus(status);
        }

        if (price != null) {
            booking.setPrice(price);
        }

        return bookingRepository.save(booking);
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

        Booking selectedBooking = bookingLotteryService.conductLottery(overlappingBookings);
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
