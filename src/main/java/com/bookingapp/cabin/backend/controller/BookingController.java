package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.service.AuthService;
import com.bookingapp.cabin.backend.service.BookingLotteryService;
import com.bookingapp.cabin.backend.service.BookingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bookingapp.cabin.backend.dtos.BookingRequestDTO;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final AuthService authService;
    private final BookingLotteryService bookingLotteryService;

    @Autowired
    public BookingController(BookingService bookingService, AuthService authService, BookingLotteryService bookingLotteryService) {
        this.bookingService = bookingService;
        this.authService = authService;
        this.bookingLotteryService = bookingLotteryService;
    }

    //oppretter en booking
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestHeader("Authorization") String firebaseToken,
                                           @RequestBody BookingRequestDTO bookingRequest) {
        try {
            String idToken = firebaseToken.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            Long userId = bookingService.getUserIdByFirebaseUid(decodedToken.getUid());

            if (userId == null) {
                return ResponseEntity.status(404).body("Bruker ikke funnet i databasen");
            }

            Booking newBooking = bookingService.createBooking(
                    userId,
                    bookingRequest.getCabinId(),
                    bookingRequest.getStartDate(),
                    bookingRequest.getEndDate(),
                    bookingRequest.getNumberOfGuests()
            );

            return ResponseEntity.ok(newBooking);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Kunne ikke opprette booking: " + e.getMessage());
        }
    }

    //prossess for å booke en spesifik hytte
    @PostMapping("/process/{cabinId}")
    public ResponseEntity<?> processBookings(@PathVariable Long cabinId,
                                             @RequestBody BookingRequestDTO bookingRequest) {
        try {
            bookingLotteryService.processBookings(cabinId, bookingRequest.getStartDate(), bookingRequest.getEndDate());
            return ResponseEntity.ok("Bookinger prosessert for hytte " + cabinId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Feil ved prosessering av bookinger: " + e.getMessage());
        }
    }

    //legge til metode for å endre en booking


    //Kansellerer min booking
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelOwnBooking(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long bookingId) {
        try {
            String idToken = authorizationHeader.replace("Bearer ", "");
            Users user = authService.authenticateUser(idToken);

            bookingService.cancelMyBooking(bookingId, user.getFirebaseUid());
            return ResponseEntity.ok("Booking kansellert: " + bookingId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Feil ved kansellering av booking: " + e.getMessage());
        }
    }

}
