package com.saptarshi.doubtconnect.dto;

import lombok.Data;

@Data
public class SessionRequestResponseDto {
    private Long id;

    private String subject;

    private String description;

    private String status;

    private int sessionDuration;

    private double totalAmount;

    private Long studentId;

    private String studentName;

    private String studentProfilePictureUrl;

    private Long teacherId;

    private String teacherName;

    private String teacherProfilePictureUrl;
}
