package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.config.AdminValidator;
import com.bookingapp.cabin.backend.dtos.AdminCreateBookingDTO;
import com.bookingapp.cabin.backend.dtos.BookingRequestDTO;
import com.bookingapp.cabin.backend.dtos.LotteryDatesRequestDTO;
import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.service.AdminService;
import com.bookingapp.cabin.backend.dtos.AdminBookingRequestDTO;
import com.bookingapp.cabin.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
//denne er clean

//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final AdminValidator adminValidator;


    private ResponseEntity<?> authorizeAdmin(String firebaseToken) {
        if (!adminValidator.isAdminEmail(firebaseToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kun admin har tilgang");
        }
        return null;
    }

    @GetMapping("/all-cabins")
    public ResponseEntity<?> getAllCabins(@RequestHeader("Authorization") String firebaseToken) {
        ResponseEntity<?> auth = authorizeAdmin(firebaseToken);
        if (auth != null){
            return auth;
        }
        return ResponseEntity.ok(adminService.getAllCabins());
    }

    //denne funker
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String firebaseToken) {
        ResponseEntity<?> auth = authorizeAdmin(firebaseToken);
        if (auth != null) {
            return auth;
        }
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    //denne funker
    // Hent en bruker basert på email – kun tilgjengelig for admin
    @GetMapping("/{email}")
    public ResponseEntity<?> getUserByEmail(@RequestHeader("Authorization") String firebaseToken,
                                            @PathVariable String email) {
            ResponseEntity<?> auth = authorizeAdmin(firebaseToken);
            if (auth != null) {
                return auth;
            }
        return adminService.getUserByEmail(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Bruker ikke funnet"));
    }

    //denne funker
    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings(@RequestHeader("Authorization") String firebaseToken) {
        ResponseEntity<?> auth = authorizeAdmin(firebaseToken);
        if (auth != null) {
            return auth;
        }
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    //denne funker
    @GetMapping("/bookings/{id}")
    public ResponseEntity<?> getBooking(@RequestHeader("Authorization") String firebaseToken, @PathVariable Long id) {
        ResponseEntity<?> auth = authorizeAdmin(firebaseToken);
        if (auth != null) {
            return auth;
        }
        return ResponseEntity.ok(adminService.getBookingById(id));
    }


    //denne funker
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<?> deleteBooking(
            @RequestHeader("Authorization") String firebaseToken, @PathVariable Long id) {
        ResponseEntity<?> auth = authorizeAdmin(firebaseToken);
        if (auth != null) {
            return auth;
        }
        adminService.deleteBooking(id);
        return ResponseEntity.ok("Booking slettet: " + id);
    }

    //denne må fikses, denne koden skal være i service, mens controller skal bare vise infoen
    //denne funker
    @PutMapping("/edit-booking/{bookingId}")
    public ResponseEntity<?> editBooking(
            @RequestHeader("Authorization") String firebaseToken,
            @PathVariable Long bookingId,
            @RequestBody AdminBookingRequestDTO request) {

        ResponseEntity<?> auth = authorizeAdmin(firebaseToken);
        if (auth != null) {
            return auth;
        }
        Booking updatedBooking = adminService.editBooking(bookingId, request);
        return ResponseEntity.ok(updatedBooking);
    }


    //lager bookinger for brukere
    @PostMapping("/bookings")
    public ResponseEntity<?> createBookingForUser(
            @RequestHeader("Authorization") String firebaseToken,
            @RequestBody AdminCreateBookingDTO request) {

        ResponseEntity<?> auth = authorizeAdmin(firebaseToken);
        if (auth != null) {
            return auth;
        }

        Booking booking = adminService.createBookingForUser(request);
        return ResponseEntity.ok(booking);
    }


    @PostMapping("/bookings-by-period")
    public ResponseEntity<?> getBookingsByPeriod(
            @RequestBody LotteryDatesRequestDTO dates,
            @RequestHeader("Authorization") String firebaseToken) {

        ResponseEntity<?> auth = authorizeAdmin(firebaseToken);
        if (auth != null) {
            return auth;
        }

        List<Booking> bookings = adminService.getBookingsInPeriod(dates);
        return ResponseEntity.ok(bookings);
    }


    //denne funker
    //prossess for å booke en spesifik hytte
    @PostMapping("/process/{cabinId}")
    public ResponseEntity<?> processBookings(@RequestHeader("Authorization") String firebaseToken, @PathVariable Long cabinId, @RequestBody BookingRequestDTO bookingRequest) {
        ResponseEntity<?> auth = authorizeAdmin(firebaseToken);
        if (auth != null) {
            return auth;
        }

        List<Booking> winners = adminService.processBookings(cabinId, bookingRequest);
        if (winners.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(winners);

    }


}


