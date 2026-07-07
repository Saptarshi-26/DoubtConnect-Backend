package com.saptarshi.doubtconnect.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "teacher_meeting_details")
public class TeacherMeetingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "teacher_profile_id", nullable = false, unique = true)
    private TeacherProfile teacherProfile;

    @Column(nullable = false)
    private String meetingPlatform;

    @Column(nullable = false)
    private String meetingLink;
}