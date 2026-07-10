package com.saptarshi.doubtconnect.dto;

import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Data
public class SignUpRequest {

    private String role;

    private String grade;
    private String language;
    private String board;

    private String bio;
    private List<String> subjects;
    private double ratePerThirtyMin;

    private String googleIdToken;

}
