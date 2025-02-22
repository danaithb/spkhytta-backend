package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
//Backend gir Custom Token til frontend
    public String authenticateUser(String email, String password) {
        //finner brukeren i mysql
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Bruker ikke funnet"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Feil passord");
        }
        //lager firebase token
        try {
            return FirebaseAuth.getInstance().createCustomToken(user.getFirebaseUid());
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Kunne ikke generere Firebase-token", e);
        }
    }
}