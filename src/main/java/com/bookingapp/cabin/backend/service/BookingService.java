package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Cabin;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.bookingapp.cabin.backend.repository.CabinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CabinRepository cabinRepository;
    private final BookingLotteryService bookingLotteryService;
    private final WaitListService waitListService;
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            CabinRepository cabinRepository,
            BookingLotteryService bookingLotteryService,
            WaitListService waitListService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.cabinRepository = cabinRepository;
        this.bookingLotteryService = bookingLotteryService;
        this.waitListService = waitListService;
    }

    //Henter bruker-id fra Firebase uid
    public Long getUserIdByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(Users::getUserId)
                .orElse(null);
    }

    //Henter alle bookinger
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    //hente bruker sine bookinger
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUser_UserId(userId);
    }

    //Oppretter en ny booking
    public Booking createBooking(Long userId, Long cabinId, LocalDate startDate, LocalDate endDate, int numberOfGuests) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Bruker ikke funnet"));

        Cabin cabin = cabinRepository.findById(cabinId)
                .orElseThrow(() -> new RuntimeException("Hytte ikke funnet"));

        int totalCost = calculateBookingCost(startDate, endDate);
        int userPointsBefore = user.getPoints();
        boolean isRestBooking = LocalDate.now().isAfter(startDate.minusWeeks(1));

        if (!isRestBooking && user.getPoints() < totalCost) {
            throw new RuntimeException("Ikke nok poeng tilgjengelig for booking");
        }

        if (!isRestBooking) {
            Booking lastBooking = bookingRepository.findTopByUser_UserIdAndStatusOrderByEndDateDesc(userId, "confirmed");
            if (lastBooking != null) {
                LocalDate requiredDate = lastBooking.getEndDate().plusDays(60);
                if (startDate.isBefore(requiredDate)) {
                    throw new RuntimeException("Må vente 60 dager etter forrige booking pga karantenetid");
                }
            }
        }

        Booking booking = new Booking(user, cabin, startDate, endDate, "pending");
        booking.setBookingCreatedDate(LocalDateTime.now());
        booking.setRestBooking(isRestBooking);
        booking.setNumberOfGuests(numberOfGuests);

        booking.setPointsBefore(userPointsBefore);
        booking.setPointsRequired(totalCost);
        booking.setPointsDeducted(0);
        booking.setPointsAfter(userPointsBefore);

        //lager unik bookingcode
        String code = "BOOKING-" + System.currentTimeMillis();
        booking.setBookingCode(code);

        return bookingRepository.save(booking);
    }

    private int calculateBookingCost(LocalDate startDate, LocalDate endDate) {
        int cost = 0;
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                cost += 4;
            } else {
                cost += 2;
            }
        }
        return cost;
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
