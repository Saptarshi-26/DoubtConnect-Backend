package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.AuthMessageResponse;
import com.saptarshi.doubtconnect.dto.AuthResponse;
import com.saptarshi.doubtconnect.dto.LoginRequest;
import com.saptarshi.doubtconnect.dto.SignUpRequest;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.google.EmailService;
import com.saptarshi.doubtconnect.google.GoogleIdentityService;
import com.saptarshi.doubtconnect.repository.StudentProfileRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
import com.saptarshi.doubtconnect.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private GoogleIdentityService googleIdentityService;

    @Autowired
    private EmailService emailService;

    public AuthResponse login(LoginRequest loginRequest){
        Optional<User> userOpt =
                userRepository.findByUsername(
                        loginRequest.getUsername());

        if(userOpt.isEmpty()){
            return null;
        }

        User user = userOpt.get();

        if(!passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword())){

            return null;
        }

        String token = jwtUtil.generateToken(user.getUsername());
        Long profileId = null;

        if(user.getRole().equals("STUDENT")){
            profileId = studentProfileRepository.findByUser(user)
                    .map(StudentProfile::getId)
                    .orElse(null);
        } else if(user.getRole().equals("TEACHER")){
            profileId = teacherProfileRepository.findByUser(user)
                    .map(TeacherProfile::getId)
                    .orElse(null);
        }

        return new AuthResponse(token, user.getRole(), user.getUsername(), profileId);
    }

    @Transactional
    public AuthMessageResponse signUp(SignUpRequest sign){
        if (!sign.getRole().equals("STUDENT")
                && !sign.getRole().equals("TEACHER")
                && !sign.getRole().equals("ADMIN")) {

            return new AuthMessageResponse("Invalid role");
        }

        String googleEmail = null;

        if (!sign.getRole().equals("ADMIN")) {
            try {
                googleEmail = googleIdentityService.verifyAndGetEmail(
                        sign.getGoogleIdToken());
            } catch (Exception e) {
                return new AuthMessageResponse("Invalid Google verification");
            }

            if (googleEmail == null) {
                return new AuthMessageResponse("Invalid Google verification");
            }
        }
        if (sign.getRole().equals("STUDENT")
                && studentProfileRepository.findByGoogleEmail(googleEmail).isPresent()) {

            return new AuthMessageResponse("Google email is already registered as a student");
        }

        if (sign.getRole().equals("TEACHER")
                && teacherProfileRepository.findByGoogleEmail(googleEmail).isPresent()) {

            return new AuthMessageResponse("Google email is already registered as a teacher");
        }

        if(userRepository.findByUsername(sign.getUsername()).isPresent()){
            return new AuthMessageResponse("Username already exists");
        }

        User user = new User();
        user.setRole(sign.getRole());
        user.setUsername(sign.getUsername());
        user.setPassword(
                passwordEncoder.encode(
                        sign.getPassword()
                )
        );

        userRepository.save(user);

        if(user.getRole().equals("STUDENT")){
            StudentProfile studentProfile = new StudentProfile();

            studentProfile.setUser(user);
            studentProfile.setGrade(sign.getGrade());
            studentProfile.setBoard(sign.getBoard());
            studentProfile.setLanguage(sign.getLanguage());
            studentProfile.setGoogleEmail(googleEmail);
            studentProfileRepository.save(studentProfile);
            emailService.sendStudentWelcomeEmail(
                    googleEmail,
                    user.getUsername()
            );

        }
        else if(user.getRole().equals("TEACHER")){
            TeacherProfile teacherProfile = new TeacherProfile();
            teacherProfile.setUser(user);
            teacherProfile.setBio(sign.getBio());
            teacherProfile.setSubjects(sign.getSubjects());
            teacherProfile.setLanguage(sign.getLanguage());
            teacherProfile.setRatePerThirtyMin(sign.getRatePerThirtyMin());
            teacherProfile.setGoogleEmail(googleEmail);
            teacherProfileRepository.save(teacherProfile);
            emailService.sendTeacherWelcomeEmail(
                    googleEmail,
                    user.getUsername()
            );



        } else if (user.getRole().equals("ADMIN")) {
            return new AuthMessageResponse("ADMIN CREATION NOT ALLOWED");
        }

        return new AuthMessageResponse("Successfully registered");

    }
}
