package com.bookingapp.cabin.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
//DTO som sendes tilbake til frontend etter vellykket innlogging

@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDTO {
    private Integer userId;
    private String firebaseUid;
    private String name;
    private String email;
}