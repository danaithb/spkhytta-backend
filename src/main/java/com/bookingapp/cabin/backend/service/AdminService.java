package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.dtos.AdminBookingRequestDTO;
import com.bookingapp.cabin.backend.dtos.AdminCreateBookingDTO;
import com.bookingapp.cabin.backend.dtos.BookingRequestDTO;
import com.bookingapp.cabin.backend.dtos.LotteryDatesRequestDTO;
import com.bookingapp.cabin.backend.model.*;
import com.bookingapp.cabin.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

//Denne er clean, men siste metode kan deles opp
@RequiredArgsConstructor
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


    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<Users> getUserByEmail (String email) {
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

    /*public List<Booking> getBookingsInPeriod(LotteryDatesRequestDTO request) {
        return bookingRepository.findByStartDateGreaterThanEqualAndEndDateLessThanEqual(
                request.getStartDate(), request.getEndDate()
        );
    }*/
    public List<Booking> getBookingsInPeriod(LotteryDatesRequestDTO request) {
        return bookingRepository.findByStatusIgnoreCaseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                "pending", request.getEndDate(), request.getStartDate()
        );
    }



    @Transactional
    public void deleteBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        bookingLogService.recordBookingLog(booking, "deleted", "admin@admin.no");
        adminRepository.delete(booking);
        logger.info("Booking {} slettet av admin.", bookingId);
    }

    @Transactional
    public Booking editBooking(Long bookingId, AdminBookingRequestDTO request) {
        Booking booking = getBookingById(bookingId);

        if ("confirmed".equalsIgnoreCase(request.getStatus())) {
            boolean conflict = bookingRepository.findByCabin_CabinIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            booking.getCabin().getCabinId(), "confirmed", request.getEndDate(), request.getStartDate())
                    .stream()
                    .anyMatch(b -> !b.getBookingId().equals(bookingId));

            if (conflict) {
                throw new RuntimeException("Hytta er allerede booket i denne perioden.");
            }
        }

        String previousStatus = booking.getStatus();

        if ("confirmed".equalsIgnoreCase(request.getStatus())
                && booking.getTripType() == TripType.PRIVATE
                && "pending".equalsIgnoreCase(previousStatus)) {
            handlePrivateBookingConfirmation(booking);
        }

        if (request.getGuestName() != null && !request.getGuestName().isEmpty()) {
            booking.getUser().setName(request.getGuestName());
            userRepository.save(booking.getUser());
        }

        if (request.getStartDate() != null) { booking.setStartDate(request.getStartDate()); }
        if (request.getEndDate() != null) { booking.setEndDate(request.getEndDate()); }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            booking.setStatus(request.getStatus());
        }
        if (request.getPrice() != null) { booking.setPrice(request.getPrice()); }

        Booking saved = bookingRepository.save(booking);
        bookingLogService.recordBookingLog(saved, "edited", "admin@admin.no");
        logger.info("Booking {} ble redigert av admin.", saved.getBookingId());
        return saved;
    }

    private void handlePrivateBookingConfirmation(Booking booking) {
        Users user = booking.getUser();
        int cost = booking.getPointsRequired();

        if (user.getPoints() < cost) {
            throw new RuntimeException("Brukeren har ikke nok poeng");
        }

        user.setPoints(user.getPoints() - cost);
        userRepository.save(user);

        pointsTransactionsService.recordPointsTransaction(user, -cost, "admin_confirm_private_booking");
        bookingLogService.recordBookingLog(booking, "confirmed_private_by_admin", "admin@admin.no");
    }

    public Booking createBookingForUser(AdminCreateBookingDTO request) {
        return bookingService.createBooking(
                request.getUserId(),
                request.getCabinId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getNumberOfGuests(),
                request.getBusinessTrip()
        );
    }

    private List<Booking> findNonConflictingBookings(List<Booking> allBookings) {
        List<Booking> result = new ArrayList<>();
        for (Booking candidate : allBookings) {
            boolean hasConflict = allBookings.stream()
                    .filter(other -> !candidate.getBookingId().equals(other.getBookingId()))
                    .anyMatch(other ->
                            !candidate.getEndDate().isBefore(other.getStartDate()) &&
                                    !candidate.getStartDate().isAfter(other.getEndDate())
                    );

            if (!hasConflict) {
                result.add(candidate);
            }
        }

        logger.info("Fant {} konfliktsfrie bookinger", result.size());
        return result;
    }
    //DENNE MÅ DELES OPP I FLERE METODER
    // Behandler bookinger for en hytte og velger en vinner via loddtrekning
    public List<Booking> processBookings(Long cabinId, BookingRequestDTO request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        List<Booking> winners = new ArrayList<>();

        List<Booking> pendingBookings = bookingRepository.findByCabin_CabinIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                cabinId, "pending", endDate, startDate
        );

        if (pendingBookings.isEmpty()) {
            logger.info("Ingen pending bookinger funnet for hytte " + cabinId);
            return winners;

        }

        List<Booking> automaticallyConfirmed = findNonConflictingBookings(pendingBookings);
        for (Booking booking : automaticallyConfirmed) {
            booking.setStatus("confirmed");
            bookingRepository.save(booking);
            bookingLogService.recordBookingLog(booking, "confirmed_no_conflict", "admin@admin.no"
            );
        }

        pendingBookings.removeAll(automaticallyConfirmed);

        List<List<Booking>> overlappingGroups = new ArrayList<>();
        for (Booking booking : pendingBookings) {
            boolean added = false;
            for (List<Booking> group : overlappingGroups) {
                boolean overlap = group.stream().anyMatch(other ->
                        !booking.getEndDate().isBefore(other.getStartDate()) &&
                                !booking.getStartDate().isAfter(other.getEndDate())
                );
                if (overlap) {
                    group.add(booking);
                    added = true;
                    break;
                }
            }
            if (!added) {
                List<Booking> newGroup = new ArrayList<>();
                newGroup.add(booking);
                overlappingGroups.add(newGroup);
            }
        }

        for (List<Booking> group : overlappingGroups) {
            Booking selectedBooking = bookingLotteryService.conductLottery(group);
            if (selectedBooking == null) {
                continue;
            }
                Users user = selectedBooking.getUser();
                int cost = selectedBooking.getPointsRequired();

                if (user.getPoints() < cost) {
                    logger.info("Du har ikke nok poeng for å utøfre bookingen");
                    selectedBooking.setStatus("rejected_insufficient_points");
                    bookingRepository.save(selectedBooking);
                    continue;
                }


                selectedBooking.setStatus("confirmed");
                user.setPoints(user.getPoints() - cost);
                userRepository.save(user);
                pointsTransactionsService.recordPointsTransaction(user, -cost, "booking_lottery");
                bookingLogService.recordBookingLog(selectedBooking, "confirmed_lottery", "admin@admin.no");
                winners.add(selectedBooking);
                user.setQuarantineEndDate(selectedBooking.getEndDate().plusDays(60));
                userRepository.save(user);

                int queuePosition = 1;
                for (Booking other : group) {
                    if (!other.getBookingId().equals(selectedBooking.getBookingId())) {
                        other.setStatus("waitlist");
                        bookingRepository.save(other);
                        bookingLogService.recordBookingLog(other, "waitlisted", "admin@admin.no");

                        WaitlistEntry entry = new WaitlistEntry();
                        entry.setBooking(other);
                        entry.setPosition(queuePosition++);
                        entry.setCreatedAt(LocalDateTime.now());
                        waitlistEntryRepository.save(entry);

                    }
                }
            logger.info("Booking ID {} vant loddtrekningen!", selectedBooking.getBookingId());
        }

        return winners;

    }}