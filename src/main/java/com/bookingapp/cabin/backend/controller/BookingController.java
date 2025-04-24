package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.service.AuthService;
import com.bookingapp.cabin.backend.service.BookingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bookingapp.cabin.backend.dtos.BookingRequestDTO;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final AuthService authService;

    @Autowired
    public BookingController(BookingService bookingService, AuthService authService) {
        this.bookingService = bookingService;
        this.authService = authService;
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

    /*//Hente alle bookinger
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }*/

    //hente bruker sine bookinger
    @GetMapping("/mine")
    public ResponseEntity<?> getMyBookings(@RequestHeader("Authorization") String firebaseToken) {
        try {
            String idToken = firebaseToken.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            Long userId = bookingService.getUserIdByFirebaseUid(decodedToken.getUid());

            if (userId == null) {
                return ResponseEntity.status(404).body("Bruker ikke funnet i databasen");
            }

            List<Booking> myBookings = bookingService.getBookingsByUserId(userId);
            return ResponseEntity.ok(myBookings);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Kunne ikke hente bookinger: " + e.getMessage());
        }
    }


    //prossess for å booke en spesifik hytte
    @PostMapping("/process/{cabinId}")
    public ResponseEntity<?> processBookings(@PathVariable Long cabinId,
                                             @RequestBody BookingRequestDTO bookingRequest) {
        try {
            bookingService.processBookings(cabinId, bookingRequest.getStartDate(), bookingRequest.getEndDate());
            return ResponseEntity.ok("Bookinger prosessert for hytte " + cabinId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Feil ved prosessering av bookinger: " + e.getMessage());
        }
    }

}
