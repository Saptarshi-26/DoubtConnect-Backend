package com.saptarshi.doubtconnect.dto;

import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Data
public class SignUpRequest {

    // User
    private String username;
    private String password;
    private String role;    // Student Teacher

    // Student
    private String grade;
    private String language;
    private String board;

    //Teacher

    private String bio;
    private List<String> subjects;
    private double ratePerThirtyMin;

    // google token for google mail
    private String googleIdToken;


}
