package com.bookingapp.cabin.backend.repository;

import com.bookingapp.cabin.backend.model.Cabin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CabinRepository extends JpaRepository<Cabin, Long> {
    Optional<Cabin> findById(Long cabinId);
    Optional<Cabin> findByCabinName(String cabinName);
}
