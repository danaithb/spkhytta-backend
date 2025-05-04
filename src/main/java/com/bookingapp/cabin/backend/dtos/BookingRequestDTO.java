//Source:https://github.com/bogdanmarculescu/microservices2024/blob/main/ongoing/src/main/java/org/cards/ongoinground/dtos/RoundDTO.java
//Source: https://www.baeldung.com/java-dto-pattern

package com.bookingapp.cabin.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@AllArgsConstructor
@Getter
@Setter

//kanskje legge til userId her
public class BookingRequestDTO {
    private Long cabinId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfGuests;
    private boolean businessTrip;


}