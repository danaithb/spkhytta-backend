//Source:https://github.com/bogdanmarculescu/microservices2024/blob/main/ongoing/src/main/java/org/cards/ongoinground/dtos/RoundDTO.java
//Source: https://www.baeldung.com/java-dto-pattern

package com.bookingapp.cabin.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.math.BigDecimal;

//DTO brukt av admin for å redigere en eksisterende booking
@AllArgsConstructor
@Getter
@Setter
public class AdminBookingRequestDTO {
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
    private Integer queuePosition;
    private String guestName;
}

