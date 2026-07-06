package com.saptarshi.doubtconnect.dto;

import lombok.Data;

@Data
public class FeedbackDto {
    private long sessionEventId;
    private int rating;
    private String review;
}
