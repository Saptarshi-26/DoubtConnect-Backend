package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.PaymentDto;
import com.saptarshi.doubtconnect.entity.Payment;
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
        Optional<SessionRequest> request = requestRepository.findById(id);
        if (request.isPresent()) {

            if (!request.get().getStatus().equals("ACCEPTED")) return "Session request has not been accepted yet ";

            if (request.get().getPayment() == null || !request.get().getPayment().getPaymentStatus().equals("SUCCESS")) {

                if (request.get().getTotalAmount() != dto.getAmount()) return "Amount did not match";

                Payment payment = request.get().getPayment();
                if (payment == null) {
                    payment = new Payment();
                }
                payment.setPaymentMethod(dto.getPaymentMethod());
                payment.setPaymentStatus("SUCCESS");
                payment.setPaymentTime(LocalDateTime.now());
                payment.setAmount(dto.getAmount());
                payment.setTransactionId(dto.getTransactionId());
                request.get().setPayment(payment);
                paymentRepository.save(payment);
                requestRepository.save(request.get());
                return "Success";
            } else return "Payment already done ";
        }
        return "Session not found ";
    }
}
