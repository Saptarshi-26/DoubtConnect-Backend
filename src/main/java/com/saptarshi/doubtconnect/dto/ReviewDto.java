package com.saptarshi.doubtconnect.dto;

import com.saptarshi.doubtconnect.entity.SessionRequest;
import lombok.Data;

@Data
public class ReviewDto {

    private String review;
    private long SessionEventId;
}
