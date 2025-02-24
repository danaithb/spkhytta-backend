package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bookingapp.cabin.backend.dtos.AuthRequestDTO;
import com.bookingapp.cabin.backend.dtos.AuthResponseDTO;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO AuthRequest) {
        try {
            // Sender Firebase-tokenet til AuthService for verifisering
            String customToken = authService.authenticateUser(AuthRequest.getFirebaseToken());
            return ResponseEntity.ok(new AuthResponseDTO(customToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
