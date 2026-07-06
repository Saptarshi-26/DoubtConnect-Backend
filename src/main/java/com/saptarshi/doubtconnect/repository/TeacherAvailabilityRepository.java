package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.TeacherAvailability;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.*;

public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability,Long> {
    List<TeacherAvailability> findByTeacherProfileOrderByStartTimeAsc(
            TeacherProfile teacherProfile);
    Optional<TeacherAvailability> findByTeacherProfileAndStartTimeAndEndTime(
            TeacherProfile teacherProfile,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<TeacherAvailability> findByTeacherProfileAndAvailableTrueAndBookedFalse(TeacherProfile teacherProfile);

    List<TeacherAvailability> findByTeacherProfileAndEndTimeAfter(TeacherProfile teacherProfile, LocalDateTime now);

    List<TeacherAvailability> findByTeacherProfileAndAvailableTrueAndBookedFalseOrderByStartTimeAsc(TeacherProfile teacherProfile);

    List<TeacherAvailability>
    findByTeacherProfileAndBookedTrueOrderByStartTimeAsc(
            TeacherProfile teacherProfile);
    List<TeacherAvailability> findByTeacherProfileAndAvailableTrueAndBookedFalseAndStartTimeAfter(
            TeacherProfile teacherProfile,
            LocalDateTime startTime
    );
}
