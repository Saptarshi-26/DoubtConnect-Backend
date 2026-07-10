package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.SessionEventResponseDto;
import com.saptarshi.doubtconnect.dto.SessionPaymentDetailsDto;
import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.google.EmailService;
import com.saptarshi.doubtconnect.payment.entity.PayoutDetails;
import com.saptarshi.doubtconnect.payment.entity.SessionPaymentDetails;
import com.saptarshi.doubtconnect.repository.SessionEventRepository;
import com.saptarshi.doubtconnect.repository.StudentProfileRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SessionEventService {

    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private SessionEventResponseDto convertToDto(SessionEvent event) {

        SessionEventResponseDto dto = new SessionEventResponseDto();

        dto.setId(event.getId());

        dto.setSessionRequestId(
                event.getSessionRequest().getId());

        dto.setStudentId(
                event.getStudentProfile().getId());

        dto.setStudentName(
                event.getStudentProfile()
                        .getUser()
                        .getDisplayName());

        dto.setStudentProfilePictureUrl(
                event.getStudentProfile()
                        .getProfilePictureUrl());

        dto.setTeacherId(
                event.getTeacherProfile().getId());

        dto.setTeacherName(
                event.getTeacherProfile()
                        .getUser()
                        .getDisplayName());

        dto.setTeacherProfilePictureUrl(
                event.getTeacherProfile()
                        .getProfilePictureUrl());

        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setMeetLink(event.getMeetLink());
        dto.setPaymentAvailable(event.isPaymentAvailable());
        dto.setEventStatus(event.getEventStatus());
        dto.setRated(event.isRated());

        if (event.isPaymentAvailable()
                && event.getSessionPaymentDetails() != null) {

            SessionPaymentDetails payment =
                    event.getSessionPaymentDetails();

            SessionPaymentDetailsDto paymentDto =
                    new SessionPaymentDetailsDto();

            if (payment.getUpiId() != null &&
                    !payment.getUpiId().isBlank()) {

                paymentDto.setUpiId(payment.getUpiId());

            } else {

                paymentDto.setAccountHolderName(
                        payment.getAccountHolderName());

                paymentDto.setAccountNumber(
                        payment.getAccountNumber());

                paymentDto.setIfscCode(
                        payment.getIfscCode());
            }

            dto.setSessionPaymentDetailsDto(paymentDto);
        }

        return dto;
    }

    private boolean ownerShip(String username, Authentication authentication) {

        Optional<User> user = userRepository.findByUsername(authentication.getName());

        return username.equals(authentication.getName()) ||
                (user.isPresent() && user.get().getRole().equals("ADMIN"));
    }

    public Optional<SessionEventResponseDto> getSessionEventById(
            Long id,
            Authentication authentication) {

        Optional<SessionEvent> event =
                sessionEventRepository.findById(id);

        if (event.isEmpty()) {
            return Optional.empty();
        }

        SessionEvent sessionEvent = event.get();

        boolean studentOwner =
                ownerShip(

                        sessionEvent.getStudentProfile()
                                .getUser()
                                .getUsername(),authentication);

        boolean teacherOwner =
                ownerShip(

                        sessionEvent.getTeacherProfile()
                                .getUser()
                                .getUsername(),authentication);

        if (!studentOwner && !teacherOwner) {
            return Optional.empty();
        }

        return Optional.of(convertToDto(sessionEvent));
    }

    public List<SessionEventResponseDto> getStudentSessions(
            long studentId,
            Authentication authentication) {

        Optional<StudentProfile> student =
                studentProfileRepository.findById(studentId);

        if (student.isEmpty())
            return new ArrayList<>();

        if (!ownerShip(
                student.get().getUser().getUsername(),
                authentication))
            return new ArrayList<>();

        return sessionEventRepository
                .findByStudentProfileOrderByStartTimeAsc(student.get())
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<SessionEventResponseDto> getTeacherSessions(
            long teacherId,
            Authentication authentication) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherId);

        if (teacher.isEmpty())
            return new ArrayList<>();

        if (!ownerShip(
                teacher.get().getUser().getUsername(),
                authentication))
            return new ArrayList<>();

        return sessionEventRepository
                .findByTeacherProfileOrderByStartTimeAsc(teacher.get())
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<SessionEventResponseDto> getUpcomingTeacherSessions(
            long teacherId,
            Authentication authentication) {
        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherId);

        if (teacher.isEmpty())
            return new ArrayList<>();

        if (!ownerShip(
                teacher.get().getUser().getUsername(),
                authentication))
            return new ArrayList<>();

        return sessionEventRepository
                .findByTeacherProfileAndEventStatusOrderByStartTimeAsc(
                        teacher.get(),
                        "UPCOMING")
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<SessionEventResponseDto> getUpcomingStudentSessions(
            long studentId,
            Authentication authentication) {
        Optional<StudentProfile> student =
                studentProfileRepository.findById(studentId);

        if (student.isEmpty())
            return new ArrayList<>();

        if (!ownerShip(
                student.get().getUser().getUsername(),
                authentication))
            return new ArrayList<>();

        return sessionEventRepository
                .findByStudentProfileAndEventStatusOrderByStartTimeAsc(
                        student.get(),
                        "UPCOMING")
                .stream()
                .map(this::convertToDto)
                .toList();
    }


    @Scheduled(fixedRate = 30000) // Every 30 seconds
    @Transactional
    public void updateSessionEvents() {

        List<SessionEvent> sessions = sessionEventRepository.findAll();

        LocalDateTime now = LocalDateTime.now();

        for (SessionEvent session : sessions) {

            boolean updated = false;

            // Session reminder
            if (!session.isReminderSent()
                    && "UPCOMING".equals(session.getEventStatus())
                    && !now.isBefore(session.getStartTime().minusHours(24))) {

                emailService.sendSessionReminderEmail(
                        session.getStudentProfile().getGoogleEmail(),
                        session.getStudentProfile().getUser().getUsername()
                );

                emailService.sendSessionReminderEmail(
                        session.getTeacherProfile().getGoogleEmail(),
                        session.getTeacherProfile().getUser().getUsername()
                );

                session.setReminderSent(true);
                updated = true;
            }

            if ("UPCOMING".equals(session.getEventStatus())
                    && !now.isBefore(session.getStartTime())) {

                session.setEventStatus("ONGOING");
                updated = true;
            }

            if ("ONGOING".equals(session.getEventStatus())
                    && !now.isBefore(session.getEndTime())) {

                session.setEventStatus("COMPLETED");
                updated = true;
            }

            int duration = session.getSessionRequest().getSessionDuration();

            long paymentAfterMinutes = Math.round(duration * 0.15);

            if (!session.isPaymentAvailable()
                    && "ONGOING".equals(session.getEventStatus())
                    && !now.isBefore(
                    session.getStartTime().plusMinutes(paymentAfterMinutes))) {

                PayoutDetails payout =
                        session.getTeacherProfile().getPayoutDetails();

                if (payout != null
                        && "ACTIVE".equals(payout.getAccountStatus())) {

                    SessionPaymentDetails paymentDetails =
                            new SessionPaymentDetails();

                    if (payout.getUpiDetails() != null) {

                        paymentDetails.setUpiId(
                                payout.getUpiDetails().getUpiId());

                    } else if (payout.getBankDetails() != null) {

                        paymentDetails.setAccountNumber(
                                payout.getBankDetails().getAccountNumber());

                        paymentDetails.setIfscCode(
                                payout.getBankDetails().getIfscCode());

                        paymentDetails.setAccountHolderName(
                                payout.getBankDetails().getAccountHolderName());
                    }

                    session.setSessionPaymentDetails(paymentDetails);
                    session.setPaymentAvailable(true);

                    emailService.sendStudentPaymentAvailableEmail(
                            session.getStudentProfile().getGoogleEmail(),
                            session.getStudentProfile().getUser().getUsername()
                    );

                    emailService.sendTeacherPaymentAvailableEmail(
                            session.getTeacherProfile().getGoogleEmail(),
                            session.getTeacherProfile().getUser().getUsername()
                    );

                    updated = true;
                }
            }

            if (updated) {
                sessionEventRepository.save(session);
            }
        }
    }
}
