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

import java.time.LocalDate;
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

    //Oppretter en ny booking
    public Booking createBooking(Long userId, Long cabinId, LocalDate startDate, LocalDate endDate) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Bruker ikke funnet"));

        Cabin cabin = cabinRepository.findById(cabinId)
                .orElseThrow(() -> new RuntimeException("Hytte ikke funnet"));

        Booking booking = new Booking(user, cabin, startDate, endDate, "pending");
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

    //Kansellerer en booking og hånterer ventelisten
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getStatus().equals("confirmed")) {
            return;
        }

        booking.setStatus("canceled");
        bookingRepository.save(booking);

        waitListService.promoteFromWaitlist(booking.getCabin().getCabinId());
    }
}
