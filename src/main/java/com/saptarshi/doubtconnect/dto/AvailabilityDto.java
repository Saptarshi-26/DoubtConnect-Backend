package com.saptarshi.doubtconnect.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AvailabilityDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
