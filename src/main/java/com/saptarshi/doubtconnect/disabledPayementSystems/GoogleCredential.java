//package com.saptarshi.doubtconnect.google;
//
//import com.saptarshi.doubtconnect.entity.TeacherProfile;
//import jakarta.persistence.*;
//import lombok.Data;
//
//@Entity
//@Data
//public class GoogleCredential {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private long id;
//
//    @Column(nullable = false)
//    private String refreshToken;
//
//    @OneToOne
//    @JoinColumn(name = "teacherProfile_id",unique = true)
//    private TeacherProfile teacherProfile;
//}
