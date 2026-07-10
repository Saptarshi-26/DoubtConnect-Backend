package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.FeedbackDto;
import com.saptarshi.doubtconnect.dto.ReviewDto;
import com.saptarshi.doubtconnect.entity.*;
import com.saptarshi.doubtconnect.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    public String submitFeedback(FeedbackDto dto, Authentication authentication) {

        Optional<SessionEvent> eventOpt = sessionEventRepository.findById(dto.getSessionEventId());
        if (eventOpt.isEmpty()) return "Session not found";

        SessionEvent event = eventOpt.get();

        if (!event.getEventStatus().equals("COMPLETED")) return "Session not completed";
        if (event.isRated()) return "Session already rated";
        if (dto.getRating() < 1 || dto.getRating() > 5) return "Rating must be between 1 and 5";
        if (dto.getReview() == null || dto.getReview().isBlank()) return "Review cannot be empty";
        if (!ownerShip(event.getSessionRequest().getStudentProfile().getUser().getUsername(), authentication))
            return "Student mismatch";
        if (reviewRepository.findBySessionEvent(event).isPresent()) return "Review already submitted";

        Optional<TeacherProfile> teacherProfileOpt =
                teacherProfileRepository.findById(event.getSessionRequest().getTeacherProfile().getId());
        if (teacherProfileOpt.isEmpty()) return "Teacher not found";

        TeacherProfile teacherProfile = teacherProfileOpt.get();

        // --- all validation passed, now mutate. Both writes are in the same
        // transaction, so either both are saved or (on any error) both roll back.

        teacherProfile.setTotalRating(teacherProfile.getTotalRating() + dto.getRating());
        teacherProfile.setNumberOfRatings(teacherProfile.getNumberOfRatings() + 1);
        teacherProfile.setRating(
                (double) teacherProfile.getTotalRating() / teacherProfile.getNumberOfRatings());
        teacherProfileRepository.save(teacherProfile);

        event.setRated(true);
        sessionEventRepository.save(event);

        Review review = new Review();
        review.setStudentProfile(event.getSessionRequest().getStudentProfile());
        review.setLocalDate(LocalDate.now());
        review.setSessionEvent(event);
        review.setReview(dto.getReview());
        review.setTeacherProfile(event.getSessionRequest().getTeacherProfile());
        reviewRepository.save(review);

        return "Feedback submitted";
    }
}
