package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO authRequest) {
        try {
            logger.info("Bruker prøver å logge inn med Firebase-token...");
            String firebaseUid = authService.authenticateUser(authRequest.getFirebaseToken());
            logger.info("Innlogging vellykket for bruker: {}", firebaseUid);
            return ResponseEntity.ok(new AuthResponseDTO(firebaseUid));
        } catch (FirebaseAuthException e) {
            logger.error("Feil: Ugyldig Firebase-token", e);
            return ResponseEntity.status(401).body("Ugyldig Firebase-token");
        } catch (RuntimeException e) {
            logger.error("Innloggingsfeil: {}", e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Uventet feil ved innlogging", e);
            return ResponseEntity.status(500).body("En feil oppstod under innlogging");
        }
    }
}