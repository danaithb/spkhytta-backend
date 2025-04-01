package com.bookingapp.cabin.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDTO {
    private Integer userId;
    private String firebaseUid;
    private String name;
    private String email;
}