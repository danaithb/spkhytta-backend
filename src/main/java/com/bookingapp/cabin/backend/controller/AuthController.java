package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.dtos.AuthResponseDTO;
import com.bookingapp.cabin.backend.globalException.BadRequestException;
import com.bookingapp.cabin.backend.globalException.ResourceNotFoundException;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    //Autentiserer brukeren basert på Firebase-token og returnerer brukerinfo
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestHeader("Authorization") String firebaseTokenHeader) {
        try {
            String firebaseToken = firebaseTokenHeader.replace("Bearer ", "");
            Users user = authService.authenticateUser(firebaseToken);

            AuthResponseDTO response = new AuthResponseDTO(
                    user.getUserId().intValue(),
                    user.getFirebaseUid(),
                    user.getName(),
                    user.getEmail()
            );
            logger.info("Innlogging vellykket for bruker: {}", user.getFirebaseUid());
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