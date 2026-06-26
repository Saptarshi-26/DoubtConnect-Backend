package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long > {
    Optional<Payment> findByTransactionId(String transactionId);
}
