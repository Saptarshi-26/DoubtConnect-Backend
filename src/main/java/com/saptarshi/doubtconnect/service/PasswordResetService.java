package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.ForgotPasswordRequest;
import com.saptarshi.doubtconnect.dto.ResetPasswordRequest;
import com.saptarshi.doubtconnect.entity.PasswordResetToken;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.google.EmailService;
import com.saptarshi.doubtconnect.repository.PasswordResetTokenRepository;
import com.saptarshi.doubtconnect.repository.StudentProfileRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    public boolean forgotPassword(
            ForgotPasswordRequest request) {

        Optional<StudentProfile> student =
                studentProfileRepository.findByGoogleEmail(
                        request.getGoogleEmail());

        User user = null;

        if (student.isPresent()) {

            user = student.get().getUser();

        } else {

            Optional<TeacherProfile> teacher =
                    teacherProfileRepository.findByGoogleEmail(
                            request.getGoogleEmail());

            if (teacher.isEmpty()) {
                return false;
            }

            user = teacher.get().getUser();
        }

        passwordResetTokenRepository
                .findByUser(user)
                .ifPresent(passwordResetTokenRepository::delete);

        PasswordResetToken token =
                new PasswordResetToken();

        token.setUser(user);

        token.setToken(
                UUID.randomUUID().toString());

        token.setExpiry(
                LocalDateTime.now().plusMinutes(5));

        passwordResetTokenRepository.save(token);

        String link =
                frontendUrl +
                        "/reset-password?token=" +
                        token.getToken();

        emailService.sendPasswordResetEmail(
                request.getGoogleEmail(),
                link);

        return true;
    }



    @Transactional
    public boolean resetPassword(
            ResetPasswordRequest request) {

        Optional<PasswordResetToken> tokenOpt =
                passwordResetTokenRepository.findByToken(
                        request.getToken());

        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOpt.get();

        if (token.getExpiry().isBefore(LocalDateTime.now())) {

            passwordResetTokenRepository.delete(token);

            return false;
        }

        User user = token.getUser();

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()));

        userRepository.save(user);
        String email;

        Optional<StudentProfile> student =
                studentProfileRepository.findByUser(user);

        if (student.isPresent()) {

            email = student.get().getGoogleEmail();

        } else {

            Optional<TeacherProfile> teacher =
                    teacherProfileRepository.findByUser(user);

            if (teacher.isEmpty()) {
                return false;
            }

            email = teacher.get().getGoogleEmail();
        }

        emailService.sendPasswordChangedEmail(email);

        passwordResetTokenRepository.delete(token);

        return true;
    }
}
