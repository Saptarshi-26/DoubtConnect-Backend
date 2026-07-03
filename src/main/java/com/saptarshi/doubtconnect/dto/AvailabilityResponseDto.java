package com.saptarshi.doubtconnect.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AvailabilityResponseDto {

    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private boolean available;

    private boolean booked;
}