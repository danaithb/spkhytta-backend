//Source:https://github.com/bogdanmarculescu/microservices2024/blob/main/ongoing/src/main/java/org/cards/ongoinground/dtos/RoundDTO.java
//Source: https://www.baeldung.com/java-dto-pattern

package com.bookingapp.cabin.backend.dtos;

import lombok.Data;

import java.time.LocalDate;
@Data
public class LotteryDatesRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
