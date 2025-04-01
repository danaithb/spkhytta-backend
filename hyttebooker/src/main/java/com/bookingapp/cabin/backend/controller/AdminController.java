package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.service.AdminService;
import com.bookingapp.cabin.backend.dtos.AdminBookingRequestDTO;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.bookingapp.cabin.backend.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings(@RequestHeader("Authorization") String firebaseToken) {
        if (!isTokenValid(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ugyldig token");
        }
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<?> getBooking(@RequestHeader("Authorization") String firebaseToken, @PathVariable Long id) {
        if (!isTokenValid(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ugyldig token");
        }
        return ResponseEntity.ok(adminService.getBookingById(id));
    }

    @PutMapping("/bookings/{id}")
    public ResponseEntity<?> updateBooking(
            @RequestHeader("Authorization") String firebaseToken,
            @PathVariable Long id,
            @RequestBody AdminBookingRequestDTO request) {

        if (!isTokenValid(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ugyldig token");
        }

        Booking updatedBooking = adminService.updateBooking(
                id, request.getStatus(), request.getStartDate(), request.getEndDate(), request.getPrice(), request.getQueuePosition()
        );
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<?> deleteBooking(@RequestHeader("Authorization") String firebaseToken, @PathVariable Long id) {
        if (!isTokenValid(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ugyldig token");
        }
        adminService.deleteBooking(id);
        return ResponseEntity.ok("Booking slettet: " + id);
    }

    private boolean isTokenValid(String firebaseToken) {
        try {
            String idToken = firebaseToken.replace("Bearer ", "");
            FirebaseAuth.getInstance().verifyIdToken(idToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}


