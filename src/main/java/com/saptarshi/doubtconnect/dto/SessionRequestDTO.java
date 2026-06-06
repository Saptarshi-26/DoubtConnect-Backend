package com.saptarshi.doubtconnect.dto;

import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

@Data
public class SessionRequestDTO {
    private Long studentProfileId;
    private Long teacherProfileId;
    private String subject;
    private String description;
    private int sessionDuration;
}
