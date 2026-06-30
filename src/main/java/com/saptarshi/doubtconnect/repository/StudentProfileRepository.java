package com.saptarshi.doubtconnect.repository;

import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile,Long> {
    public Optional<StudentProfile> findByUser(User user);

    Optional<StudentProfile> findByUserUsername(String name);
}
