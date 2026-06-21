package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.LoginRequest;
import com.saptarshi.doubtconnect.dto.SignUpRequest;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
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

    public String login(LoginRequest loginRequest){
        Optional<User> user =
                userRepository.findByUsername(
                        loginRequest.getUsername());

        if(user.isEmpty()){
            return "USER NOT FOUND";
        }

        if(!passwordEncoder.matches(
                loginRequest.getPassword(),
                user.get().getPassword())){

            return "INVALID PASSWORD";
        }



        return jwtUtil.generateToken(
                user.get().getUsername());
    }

    @Transactional
    public String signUp(SignUpRequest sign){

        if(userRepository.findByUsername(sign.getUsername()).isPresent()){
            return "Username already exists";
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
            studentProfileRepository.save(studentProfile);
        }
        else if(user.getRole().equals("TEACHER")){
            TeacherProfile teacherProfile = new TeacherProfile();
            teacherProfile.setUser(user);
            teacherProfile.setBio(sign.getBio());
            teacherProfile.setSubjects(sign.getSubjects());
            teacherProfile.setLanguage(sign.getLanguage());
            teacherProfile.setRatePerThirtyMin(sign.getRatePerThirtyMin());
            teacherProfileRepository.save(teacherProfile);

        } else if (user.getRole().equals("ADMIN")) {
            return "ADMIN CREATION NOT ALLOWED";
        }

        return "Successfully registered ";

    }
}
