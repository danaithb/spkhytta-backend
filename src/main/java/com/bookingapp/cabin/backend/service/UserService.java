package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.dtos.BookingSummaryDTO;
import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import com.bookingapp.cabin.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final WaitListService waitListService;

    @Autowired
    public UserService(UserRepository usersRepository, BookingRepository bookingRepository, WaitListService waitListService) {
        this.userRepository = usersRepository;
        this.bookingRepository = bookingRepository;
        this.waitListService = waitListService;
    }

    /*//Henter alle brukere
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    //Henter en bruker basert på email
    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }*/

    /* //Henter en bruker basert på firebase uid
    public Optional<Users> getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid);
    }*/

    public List<BookingSummaryDTO> getMyBookingSummaries(String firebaseUid) {
        List<Booking> bookings = bookingRepository.findByUser_FirebaseUidOrderByStartDateDesc(firebaseUid);

        return bookings.stream().map(booking -> new BookingSummaryDTO(
                booking.getBookingCode(),
                booking.getCabin().getCabinName(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus()
        )).toList();
    }


    public List<Booking> getBookingsForUser(String firebaseUid) {
        return bookingRepository.findByUser_FirebaseUidOrderByStartDateDesc(firebaseUid);
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



