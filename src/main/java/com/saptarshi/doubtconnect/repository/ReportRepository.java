package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.Report;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository
        extends JpaRepository<Report, Long> {

    Optional<Report> findByStudentProfileAndTeacherProfile(
            StudentProfile studentProfile,
            TeacherProfile teacherProfile
    );

    List<Report> findByStudentProfile(
            StudentProfile studentProfile
    );

    List<Report> findByTeacherProfile(
            TeacherProfile teacherProfile
    );
    void deleteAllByTeacherProfile(TeacherProfile teacherProfile);
}