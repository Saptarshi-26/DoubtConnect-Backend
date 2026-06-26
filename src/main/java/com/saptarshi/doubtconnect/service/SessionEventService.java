package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.repository.SessionEventRepository;
import com.saptarshi.doubtconnect.repository.StudentProfileRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SessionEventService {

    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private UserRepository userRepository;

    private boolean ownerShip(String username, Authentication authentication) {

        Optional<User> user = userRepository.findByUsername(authentication.getName());

        return username.equals(authentication.getName()) ||
                (user.isPresent() && user.get().getRole().equals("ADMIN"));
    }

    public List<SessionEvent> getStudentSessions(long studentId,
                                                 Authentication authentication) {

        Optional<StudentProfile> student = studentProfileRepository.findById(studentId);

        if (student.isEmpty())
            return new ArrayList<>();

        if (!ownerShip(student.get().getUser().getUsername(), authentication))
            return new ArrayList<>();

        return sessionEventRepository.findByStudentProfile(student.get());
    }

    public List<SessionEvent> getTeacherSessions(long teacherId,
                                                 Authentication authentication) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(teacherId);

        if (teacher.isEmpty())
            return new ArrayList<>();

        if (!ownerShip(teacher.get().getUser().getUsername(), authentication))
            return new ArrayList<>();

        return sessionEventRepository.findByTeacherProfile(teacher.get());
    }

    public List<SessionEvent> getUpcomingStudentSessions(long studentId,
                                                         Authentication authentication) {

        return getStudentSessions(studentId, authentication)
                .stream()
                .filter(x -> x.getEventStatus().equals("UPCOMING"))
                .toList();
    }

    public List<SessionEvent> getUpcomingTeacherSessions(long teacherId,
                                                         Authentication authentication) {

        return getTeacherSessions(teacherId, authentication)
                .stream()
                .filter(x -> x.getEventStatus().equals("UPCOMING"))
                .toList();
    }
}
