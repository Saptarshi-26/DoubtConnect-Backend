package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionEventRepository extends JpaRepository<SessionEvent,Long> {
    List<SessionEvent> findByStudentProfile(StudentProfile studentProfile);
    List<SessionEvent> findByTeacherProfile(TeacherProfile teacherProfile);
}
