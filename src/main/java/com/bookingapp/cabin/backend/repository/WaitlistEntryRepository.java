package com.bookingapp.cabin.backend.repository;

import com.bookingapp.cabin.backend.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {
}
