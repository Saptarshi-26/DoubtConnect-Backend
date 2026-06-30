//package com.saptarshi.doubtconnect.entity;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import org.hibernate.mapping.Join;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Data
//public class TimeSlot {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private long id;
//
//    @Column(nullable = false)
//    private LocalDateTime startTime;
//
//    @Column(nullable = false)
//    private LocalDateTime endTime;
//
//    @Column(nullable = false)
//    private String slotStatus = "AVAILABLE";
//
//    @ManyToOne
//    @JoinColumn(name = "teacher_id", nullable = false)
//    private TeacherProfile teacherProfile;
//
//}
