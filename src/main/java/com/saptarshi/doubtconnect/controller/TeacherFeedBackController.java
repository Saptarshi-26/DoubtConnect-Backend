package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.FeedbackDto;
import com.saptarshi.doubtconnect.dto.ReviewDto;
import com.saptarshi.doubtconnect.service.TeacherFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedback")
public class TeacherFeedBackController {

    @Autowired
    private TeacherFeedbackService service;

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ReviewDto>> getTeacherReviews(
            @PathVariable Long teacherId) {

        return ResponseEntity.ok(
                service.getTeacherReviews(teacherId));
    }
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ReviewDto>> getStudentReviews(
            @PathVariable Long studentId,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.getStudentReviews(
                        studentId,
                        authentication));
    }

    @PostMapping("/submit")
    public ResponseEntity<String> submitFeedback(@RequestBody FeedbackDto dto,
                                                 Authentication authentication) {
        String response = service.submitFeedback(dto, authentication);
        return response.equals("Feedback submitted")
                ? new ResponseEntity<>(response, HttpStatus.OK)
                : new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
