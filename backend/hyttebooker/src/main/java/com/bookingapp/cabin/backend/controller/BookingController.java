package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.service.BookingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestHeader("Authorization") String firebaseToken,
                                           @RequestBody Map<String, Object> bookingRequest) {
        try {
            String idToken = firebaseToken.replace("Bearer ", "");

            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String firebaseUid = decodedToken.getUid();

            Long userId = bookingService.getUserIdByFirebaseUid(firebaseUid);
            if (userId == null) {
                return ResponseEntity.status(404).body("Bruker ikke funnet i databasen");
            }

            Long cabinId = ((Number) bookingRequest.get("cabinId")).longValue();
            LocalDate startDate = LocalDate.parse((String) bookingRequest.get("startDate"));
            LocalDate endDate = LocalDate.parse((String) bookingRequest.get("endDate"));

            Booking newBooking = bookingService.createBooking(userId, cabinId, startDate, endDate);
            return ResponseEntity.ok(newBooking);

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Kunne ikke opprette booking: " + e.getMessage());
        }
    }

    @PostMapping("/process/{cabinId}")
    public ResponseEntity<?> processBookings(@PathVariable Long cabinId) {
        try {
            bookingService.processBookings(cabinId, LocalDate.now());
            return ResponseEntity.ok("Bookinger prosessert for hytte " + cabinId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Feil ved prosessering av bookinger: " + e.getMessage());
        }
    }
    @GetMapping("/all")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok("Booking kansellert: " + bookingId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Feil ved kansellering av booking: " + e.getMessage());
        }
    }
}
