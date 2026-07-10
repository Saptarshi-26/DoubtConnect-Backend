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
import com.saptarshi.doubtconnect.google.GoogleUserInfo;
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
                        loginRequest.getUsername().trim());

        if(userOpt.isEmpty()){
            return null;
        }

        User user = userOpt.get();

        if(!passwordEncoder.matches(
                loginRequest.getPassword().trim(),
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

    public AuthResponse googleLogin(String googleIdToken) {
        GoogleUserInfo googleUser;

        try {
            googleUser = googleIdentityService.verify(googleIdToken);
        } catch (Exception e) {
            throw new RuntimeException("Google verification failed");
        }

        if (googleUser == null) {
            throw new RuntimeException("USER_NOT_FOUND");
        }

        Optional<StudentProfile> studentOpt =
                studentProfileRepository.findByGoogleEmail(googleUser.getEmail());

        if (studentOpt.isPresent()) {

            User user = studentOpt.get().getUser();

            String token = jwtUtil.generateToken(user.getUsername());

            return new AuthResponse(
                    token,
                    user.getRole(),
                    user.getDisplayName(),
                    studentOpt.get().getId()
            );
        }

        Optional<TeacherProfile> teacherOpt =
                teacherProfileRepository.findByGoogleEmail(googleUser.getEmail());

        if (teacherOpt.isPresent()) {

            User user = teacherOpt.get().getUser();

            String token = jwtUtil.generateToken(user.getUsername());

            return new AuthResponse(
                    token,
                    user.getRole(),
                    user.getDisplayName(),
                    teacherOpt.get().getId()
            );
        }

        throw new RuntimeException("USER_NOT_FOUND");
    }

    @Transactional
    public AuthMessageResponse signUp(SignUpRequest sign) {

        if (!sign.getRole().equals("STUDENT")
                && !sign.getRole().equals("TEACHER")
                && !sign.getRole().equals("ADMIN")) {

            return new AuthMessageResponse("Invalid role");
        }

        GoogleUserInfo googleUser;

        if (!sign.getRole().equals("ADMIN")) {

            try {
                googleUser = googleIdentityService.verify(
                        sign.getGoogleIdToken());

            } catch (Exception e) {
                return new AuthMessageResponse("Invalid Google verification");
            }

            if (googleUser == null) {
                return new AuthMessageResponse("Invalid Google verification");
            }

        } else {
            return new AuthMessageResponse("ADMIN CREATION NOT ALLOWED");
        }

        if (sign.getRole().equals("STUDENT")
                && studentProfileRepository
                .findByGoogleEmail(googleUser.getEmail())
                .isPresent()) {

            return new AuthMessageResponse(
                    "Google email is already registered as a student");
        }

        if (sign.getRole().equals("TEACHER")
                && teacherProfileRepository
                .findByGoogleEmail(googleUser.getEmail())
                .isPresent()) {

            return new AuthMessageResponse(
                    "Google email is already registered as a teacher");
        }

        if (sign.getRole().equals("STUDENT")
                && (sign.getGrade() == null || sign.getGrade().isBlank()
                || sign.getBoard() == null || sign.getBoard().isBlank()
                || sign.getLanguage() == null || sign.getLanguage().isBlank())) {

            return new AuthMessageResponse("Grade, board, and language are required");
        }

        if (sign.getRole().equals("TEACHER")
                && (sign.getBio() == null || sign.getBio().isBlank()
                || sign.getLanguage() == null || sign.getLanguage().isBlank()
                || sign.getSubjects() == null || sign.getSubjects().isEmpty())) {

            return new AuthMessageResponse("Bio, language, and at least one subject are required");
        }

        User user = new User();

        user.setRole(sign.getRole());

        // Internal username
        user.setUsername(
                "dc_" + java.util.UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
        );
        user.setDisplayName(googleUser.getName());

        // Random internal password
        user.setPassword(
                passwordEncoder.encode(
                        java.util.UUID.randomUUID().toString()
                )
        );

        userRepository.save(user);

        if (user.getRole().equals("STUDENT")) {

            StudentProfile studentProfile = new StudentProfile();

            studentProfile.setUser(user);
            studentProfile.setGrade(sign.getGrade().trim());
            studentProfile.setBoard(sign.getBoard().trim());
            studentProfile.setLanguage(sign.getLanguage().trim());

            studentProfile.setGoogleEmail(
                    googleUser.getEmail()
            );

            studentProfile.setProfilePictureUrl(
                    googleUser.getPicture()
            );

            studentProfileRepository.save(studentProfile);

            emailService.sendStudentWelcomeEmail(
                    googleUser.getEmail(),
                    googleUser.getName()
            );

        }

        else if (user.getRole().equals("TEACHER")) {

            TeacherProfile teacherProfile =
                    new TeacherProfile();

            teacherProfile.setUser(user);

            teacherProfile.setBio(sign.getBio().trim());

            teacherProfile.setSubjects(
                    sign.getSubjects()
                            .stream()
                            .map(String::trim)
                            .toList()
            );

            teacherProfile.setLanguage(
                    sign.getLanguage()
            );

            teacherProfile.setRatePerThirtyMin(
                    sign.getRatePerThirtyMin()
            );

            teacherProfile.setGoogleEmail(
                    googleUser.getEmail()
            );

            teacherProfile.setProfilePictureUrl(
                    googleUser.getPicture()
            );

            teacherProfileRepository.save(
                    teacherProfile
            );

            emailService.sendTeacherWelcomeEmail(
                    googleUser.getEmail(),
                    googleUser.getName()
            );
        }

        return new AuthMessageResponse(
                "Successfully registered"
        );
    }


}
