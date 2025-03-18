package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.globalException.BadRequestException;
import com.bookingapp.cabin.backend.globalException.ResourceNotFoundException;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Users authenticateUser(String firebaseToken) throws BadRequestException, ResourceNotFoundException {
        try {
            // 1. Valider Firebase-token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String email = decodedToken.getEmail();
            logger.info("Forsøker å autentisere bruker: {}", email);

            // 2. Hent bruker fra database
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Bruker ikke funnet: " + email));

            // 3. Oppdater Firebase UID hvis den mangler (valgfritt)
            if (user.getFirebaseUid() == null) {
                user.setFirebaseUid(decodedToken.getUid());
                userRepository.save(user);
                logger.info("Oppdatert Firebase UID for bruker: {}", email);
            }

            return user;

        } catch (FirebaseAuthException e) {
            logger.error("Firebase-validering feilet: {}", e.getMessage());
            throw new BadRequestException("Ugyldig Firebase-token: " + e.getMessage());
        }
    }
}