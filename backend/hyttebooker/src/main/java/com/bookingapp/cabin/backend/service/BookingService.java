package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Cabin;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.bookingapp.cabin.backend.repository.CabinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CabinRepository cabinRepository;

    @Autowired
    private BookingLotteryService bookingLotteryService;

    @Autowired
    private WaitListService waitListService;

    public Long getUserIdByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(Users::getUserId)
                .orElse(null);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking createBooking(Long userId, Long cabinId, LocalDate startDate, LocalDate endDate) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cabin cabin = cabinRepository.findById(cabinId)
                .orElseThrow(() -> new RuntimeException("Cabin not found"));

        Booking booking = new Booking(user, cabin, startDate, endDate, "pending");
        return bookingRepository.save(booking);
    }

    public void processBookings(Long cabinId, LocalDate startDate) {
        List<Booking> pendingBookings = bookingRepository.findByCabin_CabinIdAndStatusAndStartDateBetween(
                cabinId, "pending", startDate.minusDays(3), startDate.plusDays(3)
        );

        if (pendingBookings.isEmpty()) {
            System.out.println("Ingen pending bookinger funnet for hytte " + cabinId);
            return;
        }

        Booking selectedBooking = bookingLotteryService.conductLottery(pendingBookings);

        if (selectedBooking != null) {
            selectedBooking.setStatus("confirmed");
            selectedBooking.setQueuePosition(null);
            bookingRepository.save(selectedBooking);
            System.out.println("Booking ID " + selectedBooking.getBookingId() + " vant loddtrekningen!");

            int queuePosition = 1;
            for (Booking booking : pendingBookings) {
                if (!booking.equals(selectedBooking)) {
                    booking.setStatus("waitlist");
                    booking.setQueuePosition(queuePosition++);
                    bookingRepository.save(booking);
                }
            }
        }
    }

    public void cancelBooking(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) return;

        Booking booking = bookingOpt.get();

        if (!booking.getStatus().equals("confirmed")) {
            System.out.println("Booking ID " + bookingId + " er ikke confirmed, ingen ventelisteprosess.");
            return;
        }

        booking.setStatus("canceled");
        bookingRepository.save(booking);

        System.out.println("Booking ID " + bookingId + " er kansellert. Ser etter venteliste-kandidater...");

        waitListService.promoteFromWaitlist(booking.getCabin().getCabinId());
    }
}
