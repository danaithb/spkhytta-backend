package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.dtos.BookingSummaryDTO;
import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.bookingapp.cabin.backend.service.AuthService;
import com.bookingapp.cabin.backend.service.BookingService;
import com.bookingapp.cabin.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bookingapp.cabin.backend.dtos.UserInfoDTO;

import java.util.List;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    private final BookingService bookingService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, AuthService authService, BookingService bookingService, UserRepository userRepository) {
        this.userService = userService;
        this.authService = authService;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    //Hent alle brukere - må flyttes til admin
   /* @GetMapping
    public ResponseEntity<List<Users>> getAllUsers() {
        //List<Users> users = userService.getAllUsers();
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //Hent en bruker baser på email - må flyttes til admin
    @GetMapping("/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Bruker ikke funnet"));
    }*/

    @GetMapping("/my-bookings")
    public ResponseEntity<List<Booking>> getMyBookings(@RequestHeader("Authorization") String authorizationHeader) {
        String idToken = authorizationHeader.replace("Bearer ", "");
        Users user = authService.authenticateUser(idToken);

        List<Booking> myBookings = userService.getBookingsForUser(user.getFirebaseUid());
        return ResponseEntity.ok(myBookings);
    }

    //brukerinformasjon til min side
    @GetMapping("/me")
    public ResponseEntity<UserInfoDTO> getMyInfo(@RequestHeader("Authorization") String authorizationHeader) {
        String idToken = authorizationHeader.replace("Bearer ", "");
        Users user = authService.authenticateUser(idToken);

        //henter bruker fra db for å få riktig oppdatert poengsaldo
        Users dbUser = userRepository.findByFirebaseUid(user.getFirebaseUid())
                .orElseThrow(() -> new RuntimeException("Bruker ikke funnet"));

        UserInfoDTO userInfo = new UserInfoDTO(
                user.getName(),
                user.getEmail(),
                user.getPoints()
        );

        return ResponseEntity.ok(userInfo);
    }

    //litt usikker på om vi skal bruke summary eller my-bookings
    @GetMapping("/me/bookings/summary")
    public ResponseEntity<List<BookingSummaryDTO>> getMyBookingSummaries(
            @RequestHeader("Authorization") String authorizationHeader) {
        String idToken = authorizationHeader.replace("Bearer ", "");
        Users user = authService.authenticateUser(idToken);

        List<BookingSummaryDTO> summaries = userService.getMyBookingSummaries(user.getFirebaseUid());
        return ResponseEntity.ok(summaries);
    }

    //Kansellerer min booking
    @DeleteMapping("/me/bookings/{bookingId}")
    public ResponseEntity<?> cancelOwnBooking(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long bookingId) {
        try {
            String idToken = authorizationHeader.replace("Bearer ", "");
            Users user = authService.authenticateUser(idToken);

            userService.cancelMyBooking(bookingId, user.getFirebaseUid());
            return ResponseEntity.ok("Booking kansellert: " + bookingId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Feil ved kansellering av booking: " + e.getMessage());
        }
    }

}
