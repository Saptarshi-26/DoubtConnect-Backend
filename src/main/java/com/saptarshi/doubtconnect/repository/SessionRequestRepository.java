package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.SessionRequest;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.LinkedHashSet;
import java.util.List;

public interface SessionRequestRepository
        extends JpaRepository<SessionRequest, Long> {

 @Override
 @EntityGraph(attributePaths = {
         "studentProfile",
         "studentProfile.user",
         "teacherProfile",
         "teacherProfile.user",
         "images"
 })
 List<SessionRequest> findAll();

 @Override
 @EntityGraph(attributePaths = {
         "studentProfile",
         "studentProfile.user",
         "teacherProfile",
         "teacherProfile.user",
         "images"
 })
 java.util.Optional<SessionRequest> findById(Long id);

 @EntityGraph(attributePaths = {
         "studentProfile",
         "studentProfile.user",
         "teacherProfile",
         "teacherProfile.user",
         "images"
 })
 List<SessionRequest> findByStudentProfile(StudentProfile studentProfile);

 @EntityGraph(attributePaths = {
         "studentProfile",
         "studentProfile.user",
         "teacherProfile",
         "teacherProfile.user",
         "images"
 })
 List<SessionRequest> findByTeacherProfile(TeacherProfile teacherProfile);

 @EntityGraph(attributePaths = {
         "studentProfile",
         "studentProfile.user",
         "teacherProfile",
         "teacherProfile.user",
         "images"
 })
 List<SessionRequest> findByTeacherProfileAndStatus(
         TeacherProfile teacherProfile,
         String status);

 @EntityGraph(attributePaths = {
         "studentProfile",
         "studentProfile.user",
         "teacherProfile",
         "teacherProfile.user",
         "images"
 })
 List<SessionRequest> findByStudentProfileAndStatus(
         StudentProfile studentProfile,
         String status);

 boolean existsByStudentProfileAndTeacherProfileAndDescriptionAndStatus(
         StudentProfile studentProfile,
         TeacherProfile teacherProfile,
         String description,
         String status
 );

 boolean existsByTeacherProfile(TeacherProfile teacherProfile);

}

