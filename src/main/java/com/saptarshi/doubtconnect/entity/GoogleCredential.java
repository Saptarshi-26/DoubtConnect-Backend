package com.saptarshi.doubtconnect.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.checkerframework.checker.units.qual.C;

@Entity
@Data
public class GoogleCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String refreshToken;

    @OneToOne
    @JoinColumn(name = "teacherProfile_id")
    private TeacherProfile teacherProfile;
}
