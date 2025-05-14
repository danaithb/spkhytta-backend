package com.bookingapp.cabin.backend.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;


// Verifiserer tokenet med Firebase og henter e-post og sjekker om e-posten er lik admin-adressen

@Component
public class AdminValidator {

public boolean isAdminEmail(String firebaseToken) {
    try {
        String idToken = firebaseToken.replace("Bearer ", "");
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String email = decodedToken.getEmail();
        return email != null && email.equals("admin@admin.no");
    } catch (Exception e) {
        return false;
    }

}}