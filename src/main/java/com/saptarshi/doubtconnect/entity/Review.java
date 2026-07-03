package com.saptarshi.doubtconnect.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String review;

    @OneToOne
    @JoinColumn(name="sessionEvent_id")
    private SessionEvent sessionEvent;


    @ManyToOne
    @JoinColumn(name = "teacherProfile_id",nullable = false)
    private TeacherProfile teacherProfile;

    @ManyToOne
    @JoinColumn(name = "studentProfile_id", nullable = false)
    private StudentProfile studentProfile;

    @Column(nullable = false)
    private LocalDate localDate;

}
