package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//denne klassen er fikset
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String authenticateUser(String firebaseToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String firebaseUid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            logger.info("Firebase UID: {}", firebaseUid);
            logger.info("Bruker-email: {}", email);

            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Bruker ikke funnet"));

            if (user.getFirebaseUid() == null || user.getFirebaseUid().isEmpty()) {
                logger.info("Setter Firebase UID for {}", email);
                user.setFirebaseUid(firebaseUid);
                userRepository.save(user);
            } else if (!user.getFirebaseUid().equals(firebaseUid)) {
                logger.error("Feil Firebase UID for bruker: {}", email);
                throw new RuntimeException("Feil Firebase UID");
            }

            return firebaseUid;
        } catch (FirebaseAuthException e) {
            logger.error("Kunne ikke autentisere bruker: {}", e.getMessage());
            throw new RuntimeException("Kunne ikke autentisere", e);
        }
    }

}




