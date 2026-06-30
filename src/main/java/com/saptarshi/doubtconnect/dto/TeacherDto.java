package com.saptarshi.doubtconnect.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeacherDto {
    private Long id;

    private String name;

    private String profilePictureUrl;

    private List<String> subjects;

    private String language;

    private String bio;

    private double ratePerThirtyMin;

    private double rating;

    private int numberOfRatings;

    private String  paymentMethod;
}
