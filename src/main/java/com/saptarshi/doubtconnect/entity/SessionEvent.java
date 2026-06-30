package com.saptarshi.doubtconnect.entity;

import com.saptarshi.doubtconnect.payment.entity.SessionPaymentDetails;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "session_event",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "teacherProfile_id",
                                "startTime"
                        }
                )
        }
)
public class SessionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne
    private SessionRequest sessionRequest;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "session_payment_details_id")
    private SessionPaymentDetails sessionPaymentDetails;

    @Column(nullable = false)
    private boolean paymentAvailable = false;

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
