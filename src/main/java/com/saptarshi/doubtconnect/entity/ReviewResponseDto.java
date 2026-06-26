package com.saptarshi.doubtconnect.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReviewResponseDto {
    private String review;

    private String studentName;

    private LocalDate reviewDate;
}
