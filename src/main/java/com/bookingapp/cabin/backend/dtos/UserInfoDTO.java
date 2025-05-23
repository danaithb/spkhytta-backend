package com.bookingapp.cabin.backend.dtos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
public class UserInfoDTO {
    private String name;
    private String email;
    private int points;
    private LocalDate quarantineEndDate;

}




