package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.Review;
import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Long> {
    Optional<Review> findBySessionEvent(SessionEvent sessionEvent);
    List<Review> findByTeacherProfile(TeacherProfile teacherProfile);
    List<Review> findByStudentProfile(StudentProfile studentProfile);
    void deleteAllByTeacherProfile(TeacherProfile teacherProfile);
}
