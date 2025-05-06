package com.bookingapp.cabin.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "firebase_uid", nullable = false, unique = true)
    private String firebaseUid;

   @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    //12 poeng hvert år
    @Column(nullable = false)
    private int points = 12;

    @Column(name = "quarantine_end_date")
    private LocalDate quarantineEndDate;

}
