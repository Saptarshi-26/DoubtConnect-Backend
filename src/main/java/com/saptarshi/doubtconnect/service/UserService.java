package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.UpdateUserDto;
import com.saptarshi.doubtconnect.entity.SessionRequest;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.repository.SessionRequestRepository;
import com.saptarshi.doubtconnect.repository.StudentProfileRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StudentProfileRepository studentProfileRepository;

    @Autowired
    SessionRequestRepository sessionRequestRepository;

    @Autowired
    TeacherProfileRepository teacherProfileRepository;

    public Optional<User> findUser(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public boolean deleteUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            String role = user.get().getRole();
            if (role.equals("STUDENT")) {
                Optional<StudentProfile> studentProfile = studentProfileRepository.findByUser(user.get());
                if (studentProfile.isPresent()) {
                    List<SessionRequest> sessionRequests = sessionRequestRepository.findByStudentProfile(studentProfile.get());
                    sessionRequestRepository.deleteAll(sessionRequests);
                    studentProfileRepository.deleteById(studentProfile.get().getId());
                    userRepository.deleteById(user.get().getId());
                    return true;
                }
                return false;
            } else if (role.equals("TEACHER")) {
                Optional<TeacherProfile> teacherProfile = teacherProfileRepository.findByUser(user.get());
                if (teacherProfile.isPresent()) {
                    List<SessionRequest> sessionRequests = sessionRequestRepository.findByTeacherProfile(teacherProfile.get());
                    sessionRequestRepository.deleteAll(sessionRequests);
                    teacherProfileRepository.deleteById(teacherProfile.get().getId());
                    userRepository.deleteById(user.get().getId());
                    return true;
                }
                return false;

            }
        }
        return false;
    }

    public boolean updateUser(Long id, UpdateUserDto dto) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            user.get().setUsername(dto.getUsername());
            user.get().setPassword(dto.getPassword());
            userRepository.save(user.get());
            return true;
        }
        return false;
    }

}
