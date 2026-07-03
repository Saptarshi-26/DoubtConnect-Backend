package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.RatingDto;
import com.saptarshi.doubtconnect.dto.ReviewDto;
import com.saptarshi.doubtconnect.entity.*;
import com.saptarshi.doubtconnect.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TeacherFeedbackService {

    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private boolean ownerShip(String userName , Authentication authentication){
        return userName.equals(authentication.getName());
    }

    public List<ReviewDto> getTeacherReviews(Long teacherId) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherId);

        if (teacher.isEmpty()) {
            return new ArrayList<>();
        }

        return reviewRepository.findByTeacherProfile(teacher.get())
                .stream()
                .map(review -> {

                    ReviewDto dto = new ReviewDto();

                    dto.setReview(review.getReview());
                    dto.setSessionEventId(
                            review.getSessionEvent().getId());

                    return dto;

                }).toList();
    }

    public List<ReviewDto> getStudentReviews(Long studentId, Authentication authentication) {

        Optional<StudentProfile> student =
                studentProfileRepository.findById(studentId);

        if (student.isEmpty()) {
            return new ArrayList<>();
        }

        if (!ownerShip(
                student.get().getUser().getUsername(),
                authentication)) {
            return new ArrayList<>();
        }

        return reviewRepository.findByStudentProfile(student.get())
                .stream()
                .map(review -> {

                    ReviewDto dto = new ReviewDto();

                    dto.setReview(review.getReview());
                    dto.setSessionEventId(
                            review.getSessionEvent().getId());

                    return dto;

                }).toList();
    }

    @Transactional
    public String review(ReviewDto dto,Authentication authentication){
        Optional<SessionEvent> event = sessionEventRepository.findById(dto.getSessionEventId());
        if(event.isEmpty())return "Session not found";
        if (!event.get().getEventStatus().equals("COMPLETED")) {
            return "Session not completed";
        }
        if(reviewRepository.findBySessionEvent(event.get()).isPresent())
            return "Review already submitted ";
        if(!ownerShip(event.get().getSessionRequest().getStudentProfile().getUser().getUsername(),authentication))
            return "Student mismatch";
        if (dto.getReview() == null || dto.getReview().isBlank()) {
            return "Review cannot be empty";
        }
        Review review = new Review();
        review.setStudentProfile(event.get().getStudentProfile());
        review.setLocalDate(LocalDate.now());
        review.setSessionEvent(event.get());
        review.setReview(dto.getReview());
        review.setTeacherProfile(event.get().getSessionRequest().getTeacherProfile());
        reviewRepository.save(review);
        return "Review added ";


    }

    @Transactional
    public double rate(RatingDto dto ,Authentication authentication) {

        Optional< SessionEvent> event = sessionEventRepository.findById(dto.getSessionEventId());

        if (event.isPresent() && !event.get().isRated() && (dto.getRating() >= 1 && dto.getRating() <= 5)) {
            Optional<TeacherProfile> teacherProfile =
                    teacherProfileRepository.
                            findById(event.get().getSessionRequest().getTeacherProfile().getId());
            if(!ownerShip(event.get().
                    getSessionRequest().getStudentProfile().
                    getUser().getUsername(),authentication))
                 return -1;
            if (!event.get().getEventStatus().equals("COMPLETED"))
                return -1;

            if(teacherProfile.isEmpty())return -1;
            teacherProfile.get().setTotalRating(teacherProfile.get().getTotalRating() + dto.getRating());

            teacherProfile.get().setNumberOfRatings(teacherProfile.get().getNumberOfRatings() + 1);

            teacherProfile.get().setRating((double) teacherProfile.get().getTotalRating() / teacherProfile.get().getNumberOfRatings());

            teacherProfileRepository.save(teacherProfile.get());

            event.get().setRated(true);

            sessionEventRepository.save(event.get());

            return teacherProfile.get().getRating();

        }
        return -1;
    }
}
