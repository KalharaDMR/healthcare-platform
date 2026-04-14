package com.example.paymentService.repository;

import com.example.paymentService.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    Optional<Transaction> findByStripePaymentIntentId(String paymentIntentId);
    Optional<Transaction> findBySlotId(Long slotId);
}