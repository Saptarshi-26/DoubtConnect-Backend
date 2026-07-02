package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.entity.SessionRequest;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionEventRepository extends JpaRepository<SessionEvent,Long> {
    List<SessionEvent> findByStudentProfile(StudentProfile studentProfile);
    List<SessionEvent> findByTeacherProfile(TeacherProfile teacherProfile);
    Optional<SessionEvent> findBySessionRequest(SessionRequest sessionRequest);

    List<SessionEvent> findByTeacherProfileAndEventStatus(TeacherProfile teacherProfile, String upcoming);
    Optional<SessionEvent>
    findByTeacherProfileAndStartTimeAndEndTime(
            TeacherProfile teacherProfile,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
    List<SessionEvent> findByStudentProfileAndEventStatus(
            StudentProfile studentProfile,
            String eventStatus);
}
