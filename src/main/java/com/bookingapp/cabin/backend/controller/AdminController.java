package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.dtos.BookingRequestDTO;
import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.service.AdminService;
import com.bookingapp.cabin.backend.dtos.AdminBookingRequestDTO;
import com.bookingapp.cabin.backend.service.UserService;
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
    private final UserService userService;

    @Autowired
    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String firebaseToken) {
        if (!isAdminEmail(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kun admin har tilgang");
        }
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // Hent en bruker basert på email – kun tilgjengelig for admin
    @GetMapping("/{email}")
    public ResponseEntity<?> getUserByEmail(@RequestHeader("Authorization") String firebaseToken,
                                            @PathVariable String email) {
        if (!isAdminEmail(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kun admin har tilgang");
        }

        return adminService.getUserByEmail(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Bruker ikke funnet"));
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings(@RequestHeader("Authorization") String firebaseToken) {
        if (!isAdminEmail(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kun admin har tilgang");
        }
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<?> getBooking(@RequestHeader("Authorization") String firebaseToken, @PathVariable Long id) {
        if (!isAdminEmail(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kun admin har tilgang");
        }
        return ResponseEntity.ok(adminService.getBookingById(id));
    }


    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<?> deleteBooking(@RequestHeader("Authorization") String firebaseToken, @PathVariable Long id) {
        if (!isAdminEmail(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kun admin har tilgang");
        }
        adminService.deleteBooking(id);
        return ResponseEntity.ok("Booking slettet: " + id);
    }

    @PutMapping("/edit-booking/{bookingId}")
    public ResponseEntity<?> editBooking(
            @RequestHeader("Authorization") String firebaseToken,
            @PathVariable Long bookingId,
            @RequestBody AdminBookingRequestDTO request) {

        if (!isAdminEmail(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kun admin har tilgang");
        }

        try {
            Booking updatedBooking = adminService.editBooking(
                    bookingId,
                    request.getGuestName(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getStatus(),
                    request.getPrice()
            );
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Feil ved oppdatering: " + e.getMessage());
        }
    }


    //prossess for å booke en spesifik hytte
    @PostMapping("/process/{cabinId}")
    public ResponseEntity<?> processBookings(@RequestHeader("Authorization") String firebaseToken, @PathVariable Long cabinId, @RequestBody BookingRequestDTO bookingRequest) {
        if (!isAdminEmail(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kun admin har tilgang");
        }

        try {
            adminService.processBookings(cabinId, bookingRequest.getStartDate(), bookingRequest.getEndDate());
            return ResponseEntity.ok("Bookinger prosessert for hytte " + cabinId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Feil ved prosessering av bookinger: " + e.getMessage());
        }
    }

    private boolean isAdminEmail(String firebaseToken) {
        try {
            String idToken = firebaseToken.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            return email != null && email.equals("admin@admin.no");
        } catch (Exception e) {
            return false;
        }

    }


}


