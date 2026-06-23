package com.saptarshi.doubtconnect.entity;

import com.saptarshi.doubtconnect.entity.payment.Payment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "session_requests")
public class SessionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private String subject;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile studentProfile;

    @Column(nullable = false)
    private int sessionDuration;

    @Column(nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private String paymentStatus = "PENDING";

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherProfile teacherProfile;

}
