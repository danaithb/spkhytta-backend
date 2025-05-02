package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.dtos.AdminBookingRequestDTO;
import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Cabin;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.model.WaitlistEntry;
import com.bookingapp.cabin.backend.repository.*;
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
    private final BookingService bookingService;
    private final CabinRepository cabinRepository;

    @Autowired
    public AdminService(
            AdminRepository adminRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            BookingLotteryService bookingLotteryService,
            PointsTransactionsService pointsTransactionsService,
            BookingLogService bookingLogService,
            WaitlistEntryRepository waitlistEntryRepository,
            BookingService bookingService,
            CabinRepository cabinRepository
    ) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.bookingLotteryService = bookingLotteryService;
        this.pointsTransactionsService = pointsTransactionsService;
        this.bookingLogService = bookingLogService;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.bookingService = bookingService;
        this.cabinRepository = cabinRepository;
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

    public List<Cabin> getAllCabins() {
        return cabinRepository.findAll();
    }

    public List<Booking> getBookingsInPeriod(LocalDate start, LocalDate end) {
        return bookingRepository.findByStartDateGreaterThanEqualAndEndDateLessThanEqual(start, end);
    }


    @Transactional
    public void deleteBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        bookingLogService.recordBookingLog(booking, "deleted", "admin@admin.no");
        adminRepository.delete(booking);
    }

    public Booking editBooking(Long bookingId, AdminBookingRequestDTO request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking ikke funnet"));

        if (request.getGuestName() != null && !request.getGuestName().isEmpty()) {
            Users user = booking.getUser();
            user.setName(request.getGuestName());
            userRepository.save(user);
        }

        if (request.getStartDate() != null) {
            booking.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            booking.setEndDate(request.getEndDate());
        }

        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            booking.setStatus(request.getStatus());
        }

        if (request.getPrice() != null) {
            booking.setPrice(request.getPrice());
        }

        Booking saved = bookingRepository.save(booking);
        bookingLogService.recordBookingLog(saved, "edited", "admin@admin.no");
        return saved;
    }

    public Booking createBookingForUser(Long userId, Long cabinId, LocalDate startDate, LocalDate endDate, int numberOfGuests, Boolean businessTrip) {
        return bookingService.createBooking(userId, cabinId, startDate, endDate, numberOfGuests, businessTrip);
    }

    // Behandler bookinger for en hytte og velger en vinner via loddtrekning
    public Booking processBookings(Long cabinId, LocalDate startDate, LocalDate endDate) {
        List<Booking> overlappingBookings = bookingRepository.findByCabin_CabinIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                cabinId, "pending", endDate, startDate
        );

        if (overlappingBookings.isEmpty()) {
            logger.info("Ingen pending bookinger funnet for hytte " + cabinId);
            return null;

        }

        Booking selectedBooking = bookingLotteryService.conductLottery(overlappingBookings);
        if (selectedBooking == null) {
            logger.info("Ingen booking ble valgt i loddtrekningen for hytte {}", cabinId);
            return null;
        }

        Users user = selectedBooking.getUser();
        int cost = selectedBooking.getPointsRequired();
        if (user.getPoints() < cost) {
            logger.info("Du har ikke nok poeng for å utøfre bookingen");
            selectedBooking.setStatus("rejected_insufficient_points");
            bookingRepository.save(selectedBooking);
            return null;
        }

        selectedBooking.setStatus("confirmed");
        bookingRepository.save(selectedBooking);

        selectedBooking.setStatus("confirmed");
        user.setPoints(user.getPoints() - cost);
        userRepository.save(user);
        pointsTransactionsService.recordPointsTransaction(user, -cost, "booking_lottery");
        bookingLogService.recordBookingLog(selectedBooking, "confirmed_lottery", "admin@admin.no");

        LocalDate quarantineEndDate = selectedBooking.getEndDate().plusDays(60);
        Users lotteryWinner = selectedBooking.getUser();
        lotteryWinner.setQuarantineEndDate(quarantineEndDate);
        userRepository.save(lotteryWinner);

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
        return selectedBooking;

    }




}
