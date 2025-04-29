package com.bookingapp.cabin.backend.repository;

import com.bookingapp.cabin.backend.model.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {
}
