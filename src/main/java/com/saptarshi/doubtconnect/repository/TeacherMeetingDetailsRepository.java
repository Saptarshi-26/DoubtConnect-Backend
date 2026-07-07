package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.TeacherMeetingDetails;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherMeetingDetailsRepository
        extends JpaRepository<TeacherMeetingDetails, Long> {

    Optional<TeacherMeetingDetails> findByTeacherProfile(
            TeacherProfile teacherProfile);
}