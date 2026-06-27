package com.saptarshi.doubtconnect.repository;

import com.google.auth.oauth2.GdchCredentials;
import com.saptarshi.doubtconnect.entity.GoogleCredential;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleCredentialRepository extends JpaRepository<GoogleCredential,Long> {
    Optional<GoogleCredential> findByTeacherProfile(TeacherProfile teacherProfile);
}
