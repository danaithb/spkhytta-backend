package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.dtos.UpdateGuestsDTO;
import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.service.AuthService;
import com.bookingapp.cabin.backend.service.BookingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bookingapp.cabin.backend.dtos.BookingRequestDTO;


//tenker at denne også er clean nok
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor

public class BookingController {

    private final BookingService bookingService;
    private final AuthService authService;

    //denne funker
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

            bookingService.createBooking(
                    userId,
                    bookingRequest.getCabinId(),
                    bookingRequest.getStartDate(),
                    bookingRequest.getEndDate(),
                    bookingRequest.getNumberOfGuests(),
                    bookingRequest.isBusinessTrip()
            );

            return ResponseEntity.ok("Takk for bookingen! Sjekk 'Min side' for oppdatert status.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Kunne ikke opprette booking: " + e.getMessage());
        }
    }

    //denne funker
    //legge til metode for å endre en booking
    @PutMapping("/update-guests/{bookingId}")
    public ResponseEntity<?> updateGuestCount(@RequestHeader("Authorization") String firebaseToken,
                                              @PathVariable Long bookingId,
                                              @RequestBody UpdateGuestsDTO body) {
        try {
            String idToken = firebaseToken.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            Booking updatedBooking = bookingService.updateNumberOfGuests(
                    bookingId,
                    decodedToken.getUid(),
                    body.getNewGuestCount()
            );

            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Kunne ikke oppdatere antall gjester: " + e.getMessage());
        }
    }

    //denne funker
    //Kansellerer min booking
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelOwnBooking(@RequestHeader("Authorization") String authorizationHeader,
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