//Source:https://github.com/bogdanmarculescu/microservices2024/blob/main/ongoing/src/main/java/org/cards/ongoinground/dtos/RoundDTO.java
//Source: https://www.baeldung.com/java-dto-pattern

package com.bookingapp.cabin.backend.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor

public class BookingSummaryDTO {
    private String bookingCode;
    private String cabinName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private BigDecimal price;

}
