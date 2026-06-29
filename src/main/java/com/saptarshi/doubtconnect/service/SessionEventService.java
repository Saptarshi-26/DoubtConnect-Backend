package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
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

    private boolean ownerShip(String username, Authentication authentication) {

        Optional<User> user = userRepository.findByUsername(authentication.getName());

        return username.equals(authentication.getName()) ||
                (user.isPresent() && user.get().getRole().equals("ADMIN"));
    }

    public List<SessionEvent> getStudentSessions(long studentId,
                                                 Authentication authentication) {

        Optional<StudentProfile> student = studentProfileRepository.findById(studentId);

        if (student.isEmpty())
            return new ArrayList<>();

        if (!ownerShip(student.get().getUser().getUsername(), authentication))
            return new ArrayList<>();

        return sessionEventRepository.findByStudentProfile(student.get());
    }

    public List<SessionEvent> getTeacherSessions(long teacherId,
                                                 Authentication authentication) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(teacherId);

        if (teacher.isEmpty())
            return new ArrayList<>();

        if (!ownerShip(teacher.get().getUser().getUsername(), authentication))
            return new ArrayList<>();

        return sessionEventRepository.findByTeacherProfile(teacher.get());
    }

    public List<SessionEvent> getUpcomingStudentSessions(long studentId,
                                                         Authentication authentication) {

        return getStudentSessions(studentId, authentication)
                .stream()
                .filter(x -> x.getEventStatus().equals("UPCOMING"))
                .toList();
    }

    public List<SessionEvent> getUpcomingTeacherSessions(long teacherId,
                                                         Authentication authentication) {

        return getTeacherSessions(teacherId, authentication)
                .stream()
                .filter(x -> x.getEventStatus().equals("UPCOMING"))
                .toList();
    }

    @Scheduled(fixedRate = 120000) // Every 2 minutes
    @Transactional
    public void updateSessionEvents() {

        List<SessionEvent> sessions = sessionEventRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        for (SessionEvent session : sessions) {
            boolean updated = false;
            if ("UPCOMING".equals(session.getEventStatus())
                    && !now.isBefore(session.getStartTime())) {

                session.setEventStatus("ONGOING");
                updated=true;
            }
            if ("ONGOING".equals(session.getEventStatus())
                    && !now.isBefore(session.getEndTime())) {

                session.setEventStatus("COMPLETED");
                updated=true;
            }
            int duration = session.getSessionRequest().getSessionDuration();

            long paymentAfterMinutes =
                    Math.round(duration * 0.35);

            if (!session.isPaymentAvailable()
                    && "ONGOING".equals(session.getEventStatus())
                    && !now.isBefore(
                    session.getStartTime().plusMinutes(paymentAfterMinutes))) {

                PayoutDetails payout = session.getTeacherProfile().getPayoutDetails();

                if (payout != null&&
                        "ACTIVE".equals(payout.getAccountStatus())) {


                    SessionPaymentDetails paymentDetails = new SessionPaymentDetails();

                    if (payout.getUpiDetails() != null) {

                        paymentDetails.setUpiId(
                                payout.getUpiDetails().getUpiId()
                        );

                    } else if (payout.getBankDetails() != null) {

                        paymentDetails.setAccountNumber(
                                payout.getBankDetails().getAccountNumber()
                        );

                        paymentDetails.setIfscCode(
                                payout.getBankDetails().getIfscCode()
                        );

                        paymentDetails.setAccountHolderName(
                                payout.getBankDetails().getAccountHolderName()
                        );
                    }

                    session.setSessionPaymentDetails(paymentDetails);
                    session.setPaymentAvailable(true);
                    updated = true;
                }
            }
            if(updated){
                sessionEventRepository.save(session);
            }
        }
    }
}
