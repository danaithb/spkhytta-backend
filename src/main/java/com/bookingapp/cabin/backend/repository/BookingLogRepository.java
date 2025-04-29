package com.bookingapp.cabin.backend.repository;

import com.bookingapp.cabin.backend.model.BookingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingLogRepository extends JpaRepository<BookingLog, Long> { }
