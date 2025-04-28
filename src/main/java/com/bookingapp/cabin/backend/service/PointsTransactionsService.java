package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.PointsTransaction;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.PointsTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointsTransactionsService {

    private final PointsTransactionRepository pointsTransactionRepository;

    @Autowired
    public PointsTransactionsService(PointsTransactionRepository pointsTransactionRepository) {
        this.pointsTransactionRepository = pointsTransactionRepository;
    }

    public void recordPointsTransaction(Users user, int pointsChange, String transactionType) {
        PointsTransaction transaction = new PointsTransaction();
        transaction.setUser(user);
        transaction.setPointsChange(pointsChange);
        transaction.setType(transactionType);
        pointsTransactionRepository.save(transaction);
    }
}
