//kilde repo klasser:https://github.com/bogdanmarculescu/microservices2024/blob/main/recorder/src/main/java/org/cards/recorder/model/RoundRecordRepository.java
//kilde repo klasser:https://github.com/jlwcrews2/alexandria/blob/main/src/main/java/no/jlwcrews/alexandria/location/LocationRepo.java

package com.bookingapp.cabin.backend.repository;

import com.bookingapp.cabin.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
//Repository for admin-relatert tilgang til bookinger

@Repository
public interface AdminRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatus(String status);
}
