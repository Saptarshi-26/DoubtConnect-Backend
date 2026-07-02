package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile,Long> {
    public Optional<TeacherProfile> findByUser(User user);

    Optional<TeacherProfile> findByUserUsername(String username);
}
