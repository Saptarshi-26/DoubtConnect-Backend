package com.saptarshi.doubtconnect.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderRequestDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
