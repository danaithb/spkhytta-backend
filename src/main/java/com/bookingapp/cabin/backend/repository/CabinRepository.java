package com.bookingapp.cabin.backend.repository;

import com.bookingapp.cabin.backend.model.Cabin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//Repository for cabin, håndterer datbaseoperasjoner
@Repository
public interface CabinRepository extends JpaRepository<Cabin, Long> {
}
