package com.saptarshi.doubtconnect.entity.payment;

import com.saptarshi.doubtconnect.entity.SessionRequest;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

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

    @OneToOne
    @JoinColumn(name = "session_request_id")
    private SessionRequest sessionRequest;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
