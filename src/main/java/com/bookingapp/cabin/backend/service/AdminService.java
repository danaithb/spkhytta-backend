package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.model.WaitlistEntry;
import com.bookingapp.cabin.backend.repository.AdminRepository;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.bookingapp.cabin.backend.repository.WaitlistEntryRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingLotteryService bookingLotteryService;
    private final PointsTransactionsService pointsTransactionsService;
    private final BookingLogService bookingLogService;
    private final WaitlistEntryRepository waitlistEntryRepository;


    @Autowired
    public AdminService(
            AdminRepository adminRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            BookingLotteryService bookingLotteryService,
            PointsTransactionsService pointsTransactionsService,
            BookingLogService bookingLogService,
            WaitlistEntryRepository waitlistEntryRepository
    ) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.bookingLotteryService = bookingLotteryService;
        this.pointsTransactionsService = pointsTransactionsService;
        this.bookingLogService = bookingLogService;
        this.waitlistEntryRepository = waitlistEntryRepository;
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

    @Transactional
    public void deleteBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        bookingLogService.recordBookingLog(booking, "deleted", "admin@admin.no");
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

        Booking saved = bookingRepository.save(booking);
        bookingLogService.recordBookingLog(saved, "edited", "admin@admin.no");
        return saved;
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

        Users user = selectedBooking.getUser();
        int cost = selectedBooking.getPointsRequired();
        if (user.getPoints() < cost) {
            logger.info("Du har ikke nok poeng for å utøfre bookingen");
            selectedBooking.setStatus("rejected_insufficient_points");
            bookingRepository.save(selectedBooking);
            return;
        }

        selectedBooking.setStatus("confirmed");
        bookingRepository.save(selectedBooking);

        selectedBooking.setStatus("confirmed");
        user.setPoints(user.getPoints() - cost);
        userRepository.save(user);
        pointsTransactionsService.recordPointsTransaction(user, -cost, "booking_lottery");
        bookingLogService.recordBookingLog(selectedBooking, "confirmed_lottery", "admin@admin.no");
        bookingRepository.save(selectedBooking);
        logger.info("Booking ID {} vant loddtrekningen!", selectedBooking.getBookingId());

        int queuePosition = 1;
        for (Booking booking : overlappingBookings) {
            if (!booking.getBookingId().equals(selectedBooking.getBookingId())) {
                booking.setStatus("waitlist");
                bookingRepository.save(booking);
                bookingLogService.recordBookingLog(booking, "waitlisted", "admin@admin.no");
                WaitlistEntry entry = new WaitlistEntry();
                entry.setBooking(booking);
                entry.setPosition(queuePosition++);
                entry.setCreatedAt(LocalDateTime.now());
                waitlistEntryRepository.save(entry);

            }
        }
        logger.info("Ventelisten er oppdatert for hytte {}", cabinId);
    }

}
