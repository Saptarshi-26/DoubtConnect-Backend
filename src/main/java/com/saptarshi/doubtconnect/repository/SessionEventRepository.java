package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.entity.SessionRequest;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionEventRepository extends JpaRepository<SessionEvent,Long> {
    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    List<SessionEvent> findAll();
    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    List<SessionEvent> findByStudentProfile(StudentProfile studentProfile);
    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    List<SessionEvent> findByTeacherProfile(TeacherProfile teacherProfile);
    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    Optional<SessionEvent> findBySessionRequest(SessionRequest sessionRequest);

    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    List<SessionEvent> findByTeacherProfileAndEventStatus(
            TeacherProfile teacherProfile,
            String eventStatus
    );
    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    Optional<SessionEvent> findByTeacherProfileAndStartTimeAndEndTime(
            TeacherProfile teacherProfile,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    List<SessionEvent> findByStudentProfileAndEventStatus(
            StudentProfile studentProfile,
            String eventStatus
    );
    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    List<SessionEvent> findByTeacherProfileAndEventStatusOrderByStartTimeAsc(
            TeacherProfile teacherProfile,
            String eventStatus
    );
    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    List<SessionEvent> findByStudentProfileAndEventStatusOrderByStartTimeAsc(
            StudentProfile studentProfile,
            String eventStatus
    );

    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    List<SessionEvent> findByStudentProfileOrderByStartTimeAsc(
            StudentProfile studentProfile
    );
    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    List<SessionEvent> findByTeacherProfileOrderByStartTimeAsc(
            TeacherProfile teacherProfile
    );

    @EntityGraph(attributePaths = {
            "sessionRequest",

            "studentProfile",
            "studentProfile.user",

            "teacherProfile",
            "teacherProfile.user",
            "teacherProfile.payoutDetails",
            "teacherProfile.payoutDetails.upiDetails",
            "teacherProfile.payoutDetails.bankDetails",

            "sessionPaymentDetails"
    })
    List<SessionEvent> findByEventStatusIn(List<String> statuses);

    boolean existsByTeacherProfile(TeacherProfile teacherProfile);
    boolean existsByStudentProfile(StudentProfile studentProfile);

}
