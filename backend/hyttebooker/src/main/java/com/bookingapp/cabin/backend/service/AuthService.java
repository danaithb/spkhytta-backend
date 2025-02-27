package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String authenticateUser(String firebaseToken) throws Exception {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String firebaseUid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            Optional<Users> existingUser = userRepository.findByEmail(email);

            if (existingUser.isEmpty()) {
                throw new RuntimeException("Bruker ikke funnet");
            }
            Users user = existingUser.get();

            if (user.getFirebaseUid() == null || user.getFirebaseUid().isEmpty()) {
                user.setFirebaseUid(firebaseUid);
                userRepository.save(user);
            } else if (!user.getFirebaseUid().equals(firebaseUid)) {
                throw new RuntimeException("Feil Firebase UID");
            }

            return FirebaseAuth.getInstance().createCustomToken(firebaseUid);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Kunne ikke autentisere", e);
        }
    }

}




