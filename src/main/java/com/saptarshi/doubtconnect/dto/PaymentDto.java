package com.saptarshi.doubtconnect.dto;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDto {
    private String transactionId;

    private String paymentMethod;

    private double amount;


}
