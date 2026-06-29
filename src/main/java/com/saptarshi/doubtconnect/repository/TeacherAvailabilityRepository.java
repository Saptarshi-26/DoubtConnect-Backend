package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.TeacherAvailability;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.*;

public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability,Long> {
    List<TeacherAvailability> findByTeacherProfile(TeacherProfile teacherProfile);
    Optional<TeacherAvailability> findByTeacherProfileAndStartTimeAndEndTime(
            TeacherProfile teacherProfile,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<TeacherAvailability> findByTeacherProfileAndAvailableTrueAndBookedFalse(TeacherProfile teacherProfile);
}
