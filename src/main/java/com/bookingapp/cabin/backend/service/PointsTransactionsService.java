package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.PointsTransaction;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.PointsTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
public class PointsTransactionsService {

// Tjeneste som lagrer transaksjoner knyttet til poengbruk og -endringer
    private final PointsTransactionRepository pointsTransactionRepository;

    public void recordPointsTransaction(Users user, int pointsChange, String transactionType) {
        PointsTransaction transaction = new PointsTransaction();
        transaction.setUser(user);
        transaction.setPointsChange(pointsChange);
        transaction.setType(transactionType);
        pointsTransactionRepository.save(transaction);
    }
}
