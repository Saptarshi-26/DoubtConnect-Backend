package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long > {
}
