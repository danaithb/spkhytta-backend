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

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CabinRepository cabinRepository;
    private final WaitListService waitListService;
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            CabinRepository cabinRepository,
            WaitListService waitListService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.cabinRepository = cabinRepository;
        this.waitListService = waitListService;
    }

    //skal vi ha denne????
    //Henter bruker-id fra Firebase uid
    public Long getUserIdByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(Users::getUserId)
                .orElse(null);
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

    //Kansellerer en booking og hånterer ventelisten
    public void cancelMyBooking(Long bookingId, String firebaseUid) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getFirebaseUid().equals(firebaseUid)) {
            throw new RuntimeException("Du kan kun kansellere dine egne bookinger");
        }

        if (!booking.getStatus().equals("confirmed")) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long daysSinceBooking = java.time.Duration.between(booking.getBookingCreatedDate(), now).toDays();

        if (daysSinceBooking <= 7) {
            if (!booking.isRestBooking()) {
                Users user = booking.getUser();
                int refund = booking.getPointsDeducted();
                user.setPoints(user.getPoints() + refund);
                userRepository.save(user);
            }
        } else {
            logger.info("Kansellering for sent; poeng blir ikke refundert, og bruker må betale avgift.");
        }

        booking.setStatus("canceled");
        bookingRepository.save(booking);
        waitListService.promoteFromWaitlist(booking.getCabin().getCabinId());
    }

}
