package com.saptarshi.doubtconnect.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "teacher_profiles")
public class TeacherProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ElementCollection
    private List<String > subjects= new ArrayList<>();

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String bio;

    @Column(nullable = false)
    private double ratePerThirtyMin;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;



}
