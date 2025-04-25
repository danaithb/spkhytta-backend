package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.dtos.AuthResponseDTO;
import com.bookingapp.cabin.backend.globalException.BadRequestException;
import com.bookingapp.cabin.backend.globalException.ResourceNotFoundException;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> login(@RequestHeader("Authorization") String firebaseTokenHeader) {
        try {
            logger.info("Bruker prøver å logge inn med Firebase-token...");

            // Fjern "Bearer " fra headeren
            String firebaseToken = firebaseTokenHeader.replace("Bearer ", "");

            Users user = authService.authenticateUser(firebaseToken);

            logger.info("Innlogging vellykket for bruker: {}", user.getFirebaseUid());

            AuthResponseDTO response = new AuthResponseDTO(
                    user.getUserId().intValue(),
                    user.getFirebaseUid(),
                    user.getName(),
                    user.getEmail()
            );

            return ResponseEntity.ok(response);

        } catch (BadRequestException e) {
            logger.error("Innloggingsfeil: {}", e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            logger.error("Bruker ikke funnet: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Uventet feil ved innlogging", e);
            return ResponseEntity.status(500).body("En feil oppstod under innlogging");
        }
    }
}