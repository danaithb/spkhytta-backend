package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.dtos.AuthRequestDTO;
import com.bookingapp.cabin.backend.dtos.AuthResponseDTO;
import com.bookingapp.cabin.backend.globalException.BadRequestException;
import com.bookingapp.cabin.backend.globalException.ResourceNotFoundException;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO authRequest) {
        try {
            Users user = authService.authenticateUser(authRequest.getFirebaseToken());
            AuthResponseDTO response = new AuthResponseDTO(
                    user.getUserId(),
                    user.getFirebaseUid(),
                    user.getName(),
                    user.getEmail()
            );
            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}