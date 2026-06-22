package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long > {
}
