package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.SessionRequest;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.LinkedHashSet;
import java.util.List;

public interface SessionRequestRepository extends JpaRepository<SessionRequest, Long> {
 public List<SessionRequest> findByStudentProfile(StudentProfile studentProfile);
 public List<SessionRequest> findByTeacherProfile(TeacherProfile teacherProfile);
 List<SessionRequest> findByTeacherProfileAndStatus(
         TeacherProfile teacherProfile,
         String status);

 List<SessionRequest> findByStudentProfileAndStatus(
         StudentProfile studentProfile,
         String status);
 boolean existsByStudentProfileAndTeacherProfileAndDescriptionAndStatus(
         StudentProfile studentProfile,
         TeacherProfile teacherProfile,
         String description,
         String status
 );
}
