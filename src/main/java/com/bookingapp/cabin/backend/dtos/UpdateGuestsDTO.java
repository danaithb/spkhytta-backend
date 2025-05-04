//Source:https://github.com/bogdanmarculescu/microservices2024/blob/main/ongoing/src/main/java/org/cards/ongoinground/dtos/RoundDTO.java
//Source: https://www.baeldung.com/java-dto-pattern

package com.bookingapp.cabin.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@AllArgsConstructor
@Getter
@Setter
public class UpdateGuestsDTO {
    private int newGuestCount;
}
