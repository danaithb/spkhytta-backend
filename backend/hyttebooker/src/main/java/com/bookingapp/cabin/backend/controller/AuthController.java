package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
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
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO authRequest) {
        try {
            String firebaseUid = authService.authenticateUser(authRequest.getFirebaseToken());
            return ResponseEntity.ok(new AuthResponseDTO(firebaseUid));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body("Ugyldig Firebase-token");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}