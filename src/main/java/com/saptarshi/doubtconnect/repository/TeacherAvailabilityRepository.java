package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.TeacherAvailability;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability,Long> {
    @Modifying
    @Query("""
UPDATE TeacherAvailability t
SET t.available = false
WHERE t.teacherProfile = :teacher
AND t.booked = false
""")
    int resetAvailability(@Param("teacher") TeacherProfile teacher);
    @EntityGraph(attributePaths = {
            "teacherProfile",
            "teacherProfile.user"
    })
    List<TeacherAvailability> findByTeacherProfileOrderByStartTimeAsc(
            TeacherProfile teacherProfile);
    @EntityGraph(attributePaths = {
            "teacherProfile",
            "teacherProfile.user"
    })
    Optional<TeacherAvailability> findByTeacherProfileAndStartTimeAndEndTime(
            TeacherProfile teacherProfile,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
    @EntityGraph(attributePaths = {
            "teacherProfile",
            "teacherProfile.user"
    })
    List<TeacherAvailability> findByTeacherProfileAndAvailableTrueAndBookedFalse(
            TeacherProfile teacherProfile);

    @EntityGraph(attributePaths = {
            "teacherProfile",
            "teacherProfile.user"
    })
    List<TeacherAvailability> findByTeacherProfileAndEndTimeAfter(
            TeacherProfile teacherProfile,
            LocalDateTime now);
    @EntityGraph(attributePaths = {
            "teacherProfile",
            "teacherProfile.user"
    })
    List<TeacherAvailability> findByTeacherProfileAndBookedTrueOrderByStartTimeAsc(
            TeacherProfile teacherProfile);
    @EntityGraph(attributePaths = {
            "teacherProfile",
            "teacherProfile.user"
    })
    List<TeacherAvailability> findByTeacherProfileAndAvailableTrueAndBookedFalseOrderByStartTimeAsc(TeacherProfile teacherProfile);
    @EntityGraph(attributePaths = {
            "teacherProfile",
            "teacherProfile.user"
    })
    List<TeacherAvailability> findByTeacherProfileAndAvailableTrueAndBookedFalseAndStartTimeAfter(
            TeacherProfile teacherProfile,
            LocalDateTime startTime
    );


    @Modifying
    @Query("DELETE FROM TeacherAvailability t WHERE t.teacherProfile = :teacherProfile")
    void deleteAllByTeacherProfile(@Param("teacherProfile") TeacherProfile teacherProfile);
}
