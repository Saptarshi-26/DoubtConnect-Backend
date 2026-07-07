package com.saptarshi.doubtconnect.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(
        name = "reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "student_profile_id",
                                "teacher_profile_id"
                        }
                )
        }
)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_profile_id")
    private StudentProfile studentProfile;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_profile_id")
    private TeacherProfile teacherProfile;

    @Column(nullable = false)
    private String reason;

    @Column(length = 2000)
    private String description;
}
