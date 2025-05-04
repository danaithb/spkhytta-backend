package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.dtos.BookingSummaryDTO;
import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.bookingapp.cabin.backend.service.AuthService;
import com.bookingapp.cabin.backend.service.BookingService;
import com.bookingapp.cabin.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bookingapp.cabin.backend.dtos.UserInfoDTO;
import java.util.List;

//denne er clean
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final BookingService bookingService;
    private final UserRepository userRepository;


    //denne funker
    @GetMapping("/my-bookings")
    public ResponseEntity<List<Booking>> getMyBookings(@RequestHeader("Authorization") String authorizationHeader) {
        String idToken = authorizationHeader.replace("Bearer ", "");
        Users user = authService.authenticateUser(idToken);

        List<Booking> myBookings = userService.getBookingsForUser(user.getFirebaseUid());
        return ResponseEntity.ok(myBookings);
    }

    //denne funker
    //brukerinformasjon til min side
    @GetMapping("/me")
    public ResponseEntity<UserInfoDTO> getMyInfo(@RequestHeader("Authorization") String authorizationHeader) {
        String idToken = authorizationHeader.replace("Bearer ", "");
        Users authUser = authService.authenticateUser(idToken);

        UserInfoDTO dto = userService.getMyInfo(authUser.getFirebaseUid());
        return ResponseEntity.ok(dto);
    }

    //denne funker
    @GetMapping("/me/bookings/summary")
    public ResponseEntity<List<BookingSummaryDTO>> getMyBookingSummaries(
            @RequestHeader("Authorization") String authorizationHeader) {
        String idToken = authorizationHeader.replace("Bearer ", "");
        Users user = authService.authenticateUser(idToken);

        List<BookingSummaryDTO> summaries = userService.getMyBookingSummaries(user.getFirebaseUid());
        return ResponseEntity.ok(summaries);
    }

}

