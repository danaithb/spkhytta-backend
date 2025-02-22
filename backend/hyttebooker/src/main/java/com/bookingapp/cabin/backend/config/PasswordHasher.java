package com.bookingapp.cabin.backend.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//brukes for å hashe passord, kan slettes etter at alle brukeren har fått et hashet passord
/*
public class PasswordHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode(""));
    }
}
*/