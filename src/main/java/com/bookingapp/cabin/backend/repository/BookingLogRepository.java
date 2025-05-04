//Source:https://github.com/jlwcrews2/alexandria/blob/main/src/main/java/no/jlwcrews/alexandria/location/LocationRepo.java
package com.bookingapp.cabin.backend.repository;

import com.bookingapp.cabin.backend.model.BookingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingLogRepository extends JpaRepository<BookingLog, Long> { }
