package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Cabin;
import com.bookingapp.cabin.backend.model.TripType;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.bookingapp.cabin.backend.repository.CabinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CabinRepository cabinRepository;
    private final WaitListService waitListService;
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingLogService bookingLogService;
    private final PointsTransactionsService pointsTransactionsService;

    //Henter bruker-id fra Firebase uid
    public Long getUserIdByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(Users::getUserId)
                .orElse(null);
    }


    //Oppretter en ny booking
    public Booking createBooking(Long userId, Long cabinId, LocalDate startDate, LocalDate endDate, int numberOfGuests, Boolean businessTrip) {
        LocalDate today = LocalDate.now();
        if (startDate.isBefore(today) || endDate.isBefore(today)) {
            throw new RuntimeException("Du kan ikke booke for datoer som har vært");
        }
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("Sluttdato må være etter startdato");
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Bruker ikke funnet"));

        if (user.getQuarantineEndDate() != null && user.getQuarantineEndDate().isAfter(today) && !businessTrip) {
            throw new RuntimeException("Du kan ikke booke privat under karantene. Karantene slutter: " + user.getQuarantineEndDate());
        }

        TripType tripType = Boolean.TRUE.equals(businessTrip)
                ? TripType.BUSINESS
                : TripType.PRIVATE;

        boolean available = isCabinAvailable(cabinId, startDate, endDate);
        if (!available) {
            throw new RuntimeException("Hytta er opptatt i denne perioden");
        }

        if (tripType == TripType.BUSINESS) {
            Cabin cabin = cabinRepository.findById(cabinId)
                    .orElseThrow(() -> new RuntimeException("Hytte ikke funnet"));

            Booking businessBooking = new Booking(user, cabin, startDate, endDate, "confirmed");
            businessBooking.setTripType(tripType);
            businessBooking.setNumberOfGuests(numberOfGuests);
            businessBooking.setPointsRequired(0);
            businessBooking.setPrice(BigDecimal.ZERO);
            businessBooking.setBookingCreatedDate(LocalDateTime.now());
            businessBooking.setBookingCode("BOOKING-" + System.currentTimeMillis());

            Booking savedBusinessBooking = bookingRepository.save(businessBooking);
            bookingLogService.recordBookingLog(savedBusinessBooking, "confirmed_business", user.getFirebaseUid());
            return savedBusinessBooking;
        }

        //håndterer privat booking
        int pointsCost = calculateBookingPoints(startDate, endDate);
        if (user.getPoints() < pointsCost) {
            throw new RuntimeException("Ikke nok poeng tilgjengelig for booking");
        }

        Cabin cabin = cabinRepository.findById(cabinId)
                .orElseThrow(() -> new RuntimeException("Hytte ikke funnet"));

        Booking privateBooking = new Booking(user, cabin, startDate, endDate, "pending");
        privateBooking.setBookingCreatedDate(LocalDateTime.now());
        privateBooking.setNumberOfGuests(numberOfGuests);
        privateBooking.setPointsRequired(pointsCost);
        privateBooking.setPrice(calculateBookingPrice(startDate, endDate));
        privateBooking.setBookingCode("BOOKING-" + System.currentTimeMillis());
        privateBooking.setTripType(TripType.PRIVATE);

        Booking saved = bookingRepository.save(privateBooking);
        bookingLogService.recordBookingLog(saved, "created", user.getFirebaseUid());
        return saved;
    }

    //Beregner poengkostnad for privat booking basert på antall dager
    private int calculateBookingPoints(LocalDate startDate, LocalDate endDate) {
        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return days * 3;
    }

    //Beregner pris for privat booking
    private BigDecimal calculateBookingPrice(LocalDate startDate, LocalDate endDate) {
        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return BigDecimal.valueOf(days * 500.0).setScale(2, RoundingMode.HALF_UP);
    }


    //Kansellerer en booking, hånterer ventelisten og refunderer poeng
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
            Users user = booking.getUser();
            int refund = booking.getPointsRequired();
            user.setPoints(user.getPoints() + refund);
            userRepository.save(user);
        } else {
            logger.info("Kansellering for sent; poeng blir ikke refundert, og bruker må betale avgift.");
        }

        booking.setStatus("canceled");
        bookingRepository.save(booking);
        bookingLogService.recordBookingLog(booking, "canceled", firebaseUid);
        waitListService.promoteFromWaitlist(booking.getCabin().getCabinId());
    }

    //Oppdaterer antall gjester for en booking
    public Booking updateNumberOfGuests(Long bookingId, String firebaseUid, int newNumberOfGuests) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking ikke funnet"));

        LocalDate today = LocalDate.now();
        if (booking.getStartDate().isBefore(today)) {
            throw new RuntimeException("Kan ikke endre antall gjester for bookinger som allerede har startet");
        }

        if (!booking.getUser().getFirebaseUid().equals(firebaseUid)) {
            throw new RuntimeException("Du kan kun endre dine egne bookinger");
        }

        if (!booking.getStatus().equals("confirmed")) {
            throw new RuntimeException("Kan kun endre confirmed bookinger");
        }

        booking.setNumberOfGuests(newNumberOfGuests);
        Booking updated = bookingRepository.save(booking);
        bookingLogService.recordBookingLog(updated, "update_guests", firebaseUid);
        return updated;
    }

    // Sjekker om hytta er ledig i gitt periode, basert på bookingregler og logikk for opptatte datoer
    public boolean isCabinAvailable(Long cabinId, LocalDate startDate, LocalDate endDate) {
        List<Booking> overlappingBookings = bookingRepository.findByCabin_CabinIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                cabinId, "confirmed", endDate, startDate
        );

        // Samme logikk som i CalendarService
        Map<LocalDate, Integer> bookingCounts = new HashMap<>();
        for (Booking booking : overlappingBookings) {
            LocalDate[] edgeDates = {booking.getStartDate(), booking.getEndDate()};
            for (LocalDate d : edgeDates) {
                bookingCounts.put(d, bookingCounts.getOrDefault(d, 0) + 1);
            }

            LocalDate date = booking.getStartDate().plusDays(1);
            while (date.isBefore(booking.getEndDate())) {
                bookingCounts.put(date, 2);
                date = date.plusDays(1);
            }
        }

        // Sjekk om noen av datoene i forespørselen er opptatt
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            int count = bookingCounts.getOrDefault(date, 0);
            if (count >= 2) {
                return false;
            }
            date = date.plusDays(1);
        }

        return true;
    }
}