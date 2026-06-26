package com.saptarshi.doubtconnect.entity;

import com.saptarshi.doubtconnect.entity.payment.Payment;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class SessionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne
    private SessionRequest sessionRequest;

    @OneToOne
    private Payment payment;

    @Column(nullable = false)
    private String eventStatus = "UPCOMING";

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column
    private String meetLink;

    @Column(nullable = false)
    private boolean rated = false;

    @ManyToOne
    @JoinColumn(name = "studentProfile_id")
    private StudentProfile studentProfile;

    @ManyToOne
    @JoinColumn(name = "teacherProfile_id")
    private TeacherProfile teacherProfile;

}
