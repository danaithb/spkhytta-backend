package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Cabin;
import com.bookingapp.cabin.backend.model.TripType;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.bookingapp.cabin.backend.repository.CabinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CabinRepository cabinRepository;
    private final WaitListService waitListService;
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingLogService bookingLogService;
    private final PointsTransactionsService pointsTransactionsService;

    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            CabinRepository cabinRepository,
            WaitListService waitListService,
            BookingLogService bookingLogService,
            PointsTransactionsService pointsTransactionsService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.cabinRepository = cabinRepository;
        this.waitListService = waitListService;
        this.bookingLogService = bookingLogService;
        this.pointsTransactionsService = pointsTransactionsService;
    }


    //skal vi ha denne????
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

        TripType tripType = Boolean.TRUE.equals(businessTrip)
                ? TripType.BUSINESS
                : TripType.PRIVATE;

        boolean available = isCabinAvailable(cabinId, startDate, endDate);
        if (!available) {
            throw new RuntimeException("Hytta er opptatt i denne perioden");
        }
        Cabin cabin = cabinRepository.findById(cabinId)
                    .orElseThrow(() -> new RuntimeException("Hytte ikke funnet"));

        if (tripType == TripType.BUSINESS) {
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

        LocalDate quarantineEnd = user.getQuarantineEndDate();
        if (quarantineEnd != null && quarantineEnd.isAfter(today)) {
            throw new RuntimeException("Du er i karantene til " + quarantineEnd);
        }

        Booking lastBooking = bookingRepository.findTopByUser_UserIdAndStatusOrderByEndDateDesc(userId, "confirmed");
        if (lastBooking != null) {
            LocalDate requiredDate = lastBooking.getEndDate().plusDays(60);
            if (startDate.isBefore(requiredDate)) {
                throw new RuntimeException("Må vente 60 dager etter forrige booking pga karantenetid");
            }
        }
        int pointsCost = calculateBookingPoints(startDate, endDate);
        BigDecimal price = calculateBookingPrice(startDate, endDate);

        if (user.getPoints() < pointsCost) {
            throw new RuntimeException("Ikke nok poeng tilgjengelig for booking");
        }

        Booking privateBooking = new Booking(user, cabin, startDate, endDate, "pending");
        privateBooking.setTripType(tripType);
        privateBooking.setBookingCreatedDate(LocalDateTime.now());
        privateBooking.setNumberOfGuests(numberOfGuests);
        privateBooking.setPointsRequired(pointsCost);
        privateBooking.setPrice(price);
        privateBooking.setBookingCode("BOOKING-" + System.currentTimeMillis());

        Booking saved = bookingRepository.save(privateBooking);
        bookingLogService.recordBookingLog(saved, "created", user.getFirebaseUid());
        return saved;
    }

    private int calculateBookingPoints(LocalDate startDate, LocalDate endDate) {
        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return days * 3;
    }

    private BigDecimal calculateBookingPrice(LocalDate startDate, LocalDate endDate) {
        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return BigDecimal.valueOf(days * 500.0).setScale(2, RoundingMode.HALF_UP);
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

    public Booking updateGuestCount(Long bookingId, String firebaseUid, int newGuestCount) {
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

        booking.setNumberOfGuests(newGuestCount);
        Booking updated = bookingRepository.save(booking);
        bookingLogService.recordBookingLog(updated, "update_guests", firebaseUid);
        return updated;
    }


    //backup metoder nedenfor for å få en booking med engang uten loddtrekning

    public boolean isCabinAvailable(Long cabinId, LocalDate startDate, LocalDate endDate) {
        List<Booking> overlappingBookings = bookingRepository.findByCabin_CabinIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                cabinId, "confirmed", endDate, startDate
        );
        return overlappingBookings.isEmpty();
    }
/*
    public Booking createAndConfirmBooking(Long userId, Long cabinId, LocalDate startDate, LocalDate endDate, int numberOfGuests, Boolean businessTrip) {
        LocalDate today = LocalDate.now();
        if (startDate.isBefore(today) || endDate.isBefore(today)) {
            throw new RuntimeException("Du kan ikke booke for datoer som har vært");
        }
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("Sluttdato må være etter startdato");
        }
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Bruker ikke funnet"));

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
            businessBooking.setTripType(TripType.BUSINESS);
            businessBooking.setNumberOfGuests(numberOfGuests);
            businessBooking.setPointsRequired(0);
            businessBooking.setPrice(BigDecimal.ZERO);
            businessBooking.setBookingCreatedDate(LocalDateTime.now());
            businessBooking.setBookingCode("BOOKING-" + System.currentTimeMillis());

            Booking savedBusinessBooking = bookingRepository.save(businessBooking);
            bookingLogService.recordBookingLog(savedBusinessBooking, "confirmed_business", user.getFirebaseUid());
            return savedBusinessBooking;
        }

        LocalDate quarantineEnd = user.getQuarantineEndDate();
        if (quarantineEnd != null && quarantineEnd.isAfter(today)) {
            throw new RuntimeException("Du er i karantene til " + quarantineEnd + ". Kan ikke opprette ny booking.");
        }

        int totalCost = calculateBookingPoints(startDate, endDate);
        BigDecimal totalPrice = calculateBookingPrice(startDate, endDate);
        if (user.getPoints() < totalCost) {
            throw new RuntimeException("Ikke nok poeng tilgjengelig for booking");
        }


        // Trekk poeng med en gang
        int userPointsBefore = user.getPoints();
        user.setPoints(user.getPoints() - totalCost);
        userRepository.save(user);
        pointsTransactionsService.recordPointsTransaction(user, -totalCost, "booking");

        Cabin cabin = cabinRepository.findById(cabinId)
                .orElseThrow(() -> new RuntimeException("Hytte ikke funnet"));

        Booking privateBooking = new Booking();
        privateBooking.setUser(user);
        privateBooking.setCabin(cabin);
        privateBooking.setStartDate(startDate);
        privateBooking.setEndDate(endDate);
        privateBooking.setNumberOfGuests(numberOfGuests);
        privateBooking.setStatus("confirmed");
        privateBooking.setBookingCreatedDate(LocalDateTime.now());
        privateBooking.setPointsRequired(totalCost);
        privateBooking.setPrice(totalPrice);
        privateBooking.setTripType(TripType.PRIVATE);
        privateBooking.setBookingCode("BOOKING-" + System.currentTimeMillis());

        Booking savedBooking = bookingRepository.save(privateBooking);
        bookingLogService.recordBookingLog(savedBooking, "confirmed_private", user.getFirebaseUid());

        LocalDate quarantineEndDate = savedBooking.getEndDate().plusDays(60);
        Users bookingUser = savedBooking.getUser();
        bookingUser.setQuarantineEndDate(quarantineEndDate);
        userRepository.save(bookingUser);

        return savedBooking;

    }
*/

}
