package com.saptarshi.doubtconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TeacherAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id ;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private boolean available = false;

    private boolean booked = false;

    @ManyToOne
    @JoinColumn(name = "teacher_profile_id",nullable = false)
    private TeacherProfile teacherProfile;

}
