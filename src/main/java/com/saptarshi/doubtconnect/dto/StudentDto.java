package com.saptarshi.doubtconnect.dto;

import lombok.Data;

@Data
public class StudentDto {
    private Long id;

    private String name;

    private String profilePictureUrl;

    private String grade;

    private String board;

    private String language;
}
