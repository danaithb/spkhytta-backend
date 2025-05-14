package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

//autentiserer brukere basert på Firebase-token
@RequiredArgsConstructor
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;

    //Verifiserer Firebase-token og returnerer tilhørende bruker fra databasen
    public Users authenticateUser(String firebaseToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String firebaseUid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            logger.info("Firebase UID: {}", firebaseUid);
            logger.info("Bruker-email: {}", email);

            logger.info("Prøver å finne bruker med e-post: {}", email);
            //Prøver å finne brukeren i databasen basert på e-post
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("Fant ikke bruker med e-post: {}", email);
                        return new RuntimeException("Bruker ikke funnet");
                    });
            //Oppdaterer Firebase UID hvis den mangler
            if (user.getFirebaseUid() == null || user.getFirebaseUid().isBlank()) {
                logger.info("Setter Firebase UID for {}", email);
                user.setFirebaseUid(firebaseUid);
                userRepository.save(user);
                //Sjekker at UID stemmer hvis den allerede er satt
            } else if (!user.getFirebaseUid().equals(firebaseUid)) {
                logger.error("Feil Firebase UID for bruker: {}", email);
                throw new RuntimeException("Feil Firebase UID");
            }

            return user;
        } catch (FirebaseAuthException e) {
            logger.error("Kunne ikke autentisere bruker: {}", e.getMessage());
            throw new RuntimeException("Kunne ikke autentisere", e);
        }
    }

}




