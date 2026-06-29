package com.saptarshi.doubtconnect.payment.repository;

import com.saptarshi.doubtconnect.payment.entity.PayoutDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentOutRepository extends JpaRepository<PayoutDetails,Long > {
}
