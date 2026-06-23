package com.saptarshi.doubtconnect.entity.payment;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class RazorpayOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String razorpayOrderId;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String orderStatus = "CREATED";

    @Column(nullable = false)
    private Long sessionRequestId;
}
