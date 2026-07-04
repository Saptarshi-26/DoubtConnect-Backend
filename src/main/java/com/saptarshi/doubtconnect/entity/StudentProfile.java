package com.saptarshi.doubtconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String profilePictureUrl;

    @Column(nullable = false)
    private String grade;

    @Column(nullable = false)
    private String board;

    @Column(nullable = false)
    private String language;

    @OneToMany
    private List<TeacherProfile> favourites = new ArrayList<>();

    @JoinColumn(name = "user_id", nullable = false)
    @OneToOne
    private User user;

    @Column(nullable = false, unique = true)
    private String googleEmail;




}
