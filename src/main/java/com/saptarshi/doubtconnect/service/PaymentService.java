package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.PaymentDto;
import com.saptarshi.doubtconnect.entity.payment.Payment;
import com.saptarshi.doubtconnect.entity.SessionRequest;
import com.saptarshi.doubtconnect.repository.PaymentRepository;
import com.saptarshi.doubtconnect.repository.SessionRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    SessionRequestRepository requestRepository;

    @Autowired
    PaymentRepository paymentRepository;

    public String payment(long id, PaymentDto dto) {
     return null;
    }
}
