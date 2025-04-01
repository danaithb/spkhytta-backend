package com.bookingapp.cabin.backend.repository;

import com.bookingapp.cabin.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatus(String status);
}
