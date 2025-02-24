package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Brukeren autentiseres via Firebase, og backend gir et custom token til frontend
    public String authenticateUser(String firebaseToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String firebaseUid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Bruker ikke funnet"));

            return FirebaseAuth.getInstance().createCustomToken(firebaseUid);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Kunne ikke autentisere", e);
        }
    }
}