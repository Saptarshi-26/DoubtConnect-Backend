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

    public List<ReviewResponseDto> getAll(long teacherProfileId){
        Optional<TeacherProfile> profile = teacherProfileRepository.findById(teacherProfileId);
        return profile.map(teacherProfile -> reviewRepository.findByTeacherProfile(teacherProfile).stream().map(x -> {
            ReviewResponseDto reviewResponseDto = new ReviewResponseDto();
            reviewResponseDto.setReviewDate(x.getLocalDate());
            reviewResponseDto.setStudentName(x.getSessionEvent().getStudentProfile().
                    getUser().getUsername());
            reviewResponseDto.setReview(x.getReview());
            return reviewResponseDto;

        }).toList()).orElseGet(ArrayList::new);
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
