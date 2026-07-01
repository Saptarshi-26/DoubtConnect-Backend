package com.saptarshi.doubtconnect.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionEventResponseDto {
    private Long id;

    private Long sessionRequestId;

    private Long studentId;

    private String studentName;

    private String studentProfilePictureUrl;

    private Long teacherId;

    private String teacherName;

    private String teacherProfilePictureUrl;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String meetLink;

    private boolean paymentAvailable;

    private String eventStatus;

    private boolean rated;
}
