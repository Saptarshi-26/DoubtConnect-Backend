package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.RatingDto;
import com.saptarshi.doubtconnect.dto.ReviewDto;
import com.saptarshi.doubtconnect.service.TeacherFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedback")
public class TeacherFeedBackController {

    @Autowired
    private TeacherFeedbackService service;

    @PostMapping("/review")
    public ResponseEntity<String> addReview(@RequestBody ReviewDto dto,
                                            Authentication authentication) {

        String response = service.review(dto, authentication);

        return response.equals("Review added")
                ? new ResponseEntity<>(response, HttpStatus.OK)
                : new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/rate")
    public ResponseEntity<?> rateTeacher(@RequestBody RatingDto dto , Authentication authentication){
        double rating = service.rate(dto, authentication);

        return rating == -1
                ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(rating, HttpStatus.OK);
    }
}
