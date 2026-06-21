package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.SessionActionDTO;
import com.saptarshi.doubtconnect.dto.SessionRequestDTO;
import com.saptarshi.doubtconnect.dto.UpdateSessionDTO;
import com.saptarshi.doubtconnect.entity.SessionRequest;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.repository.SessionRequestRepository;
import com.saptarshi.doubtconnect.repository.StudentProfileRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service

public class SessionRequestService {

    @Autowired
    UserRepository userRepository ;

    @Autowired
    SessionRequestRepository sessionRequestRepository;

    @Autowired
    StudentProfileRepository studentProfileRepository;

    @Autowired
    TeacherProfileRepository teacherProfileRepository;

    private boolean ownership(Authentication authentication, String username){
        Optional<User> user = userRepository.findByUsername(authentication.getName());

        return user.filter(value -> authentication.getName().equals(username) || value.getRole().equals("ADMIN")).isPresent();

    }


    @Transactional
    public boolean sendRequest(SessionRequestDTO dto, Authentication authentication){

        Optional<StudentProfile> student =
                studentProfileRepository.findById(dto.getStudentProfileId());


        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(dto.getTeacherProfileId());


        if(student.isPresent() && teacher.isPresent()) {

            if(!ownership(authentication,student.get().getUser().getUsername()))return false;

            SessionRequest request = new SessionRequest();

            request.setStatus("PENDING");
            request.setSubject(dto.getSubject());
            request.setDescription(dto.getDescription());
            request.setStudentProfile(student.get());
            request.setTeacherProfile(teacher.get());
            request.setSessionDuration(dto.getSessionDuration());
            request.setTotalAmount((dto.getSessionDuration()/30.0)*teacher.get().getRatePerThirtyMin());


            sessionRequestRepository.save(request);

            return true;
        }

        return false;
    }

    public List<SessionRequest> findByStudentProfile(Long id ,Authentication authentication){

        Optional<StudentProfile> studentProfile = studentProfileRepository.findById(id);
        if(studentProfile.isPresent()) {

            if(!ownership(authentication,studentProfile.get().getUser().getUsername()))
                return new ArrayList<>();

            return sessionRequestRepository.findByStudentProfile(studentProfile.get());
        }
        return new ArrayList<>();
    }

    public List<SessionRequest> findTeacherProfile(Long id,Authentication authentication){
        Optional<TeacherProfile> teacherProfile = teacherProfileRepository.findById(id);
        if(teacherProfile.isPresent()) {

            if(!ownership(authentication,teacherProfile.get().getUser().getUsername()))
                return new ArrayList<>();

            return sessionRequestRepository.findByTeacherProfile(teacherProfile.get());
        }
        else return new ArrayList<>();
    }

    public String acceptRequest(SessionActionDTO sessionActionDTO,Authentication authentication){
        Optional<SessionRequest> session = sessionRequestRepository.findById(sessionActionDTO.getSessionRequestId());
        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(sessionActionDTO.getTeacherProfileId());


        if(session.isPresent()&&teacher.isPresent()&&session.get().getTeacherProfile().getId().equals(teacher.get().getId())){

            if(!ownership(authentication,teacher.get().getUser().getUsername()))return "Teacher not matched ";

            if(!session.get().getStatus().equals("PENDING")){
                return "ALREADY PROCESSED";
            }

            session.get().setStatus("ACCEPTED");
            sessionRequestRepository.save(session.get());
            return "ACCEPTED";
        }
        return "Session or Teacher not found ";
    }

    public String rejectRequest(SessionActionDTO sessionActionDTO ,Authentication authentication){
        Optional<SessionRequest> session = sessionRequestRepository.findById(sessionActionDTO.getSessionRequestId());
        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(sessionActionDTO.getTeacherProfileId());

        if(session.isPresent()&&teacher.isPresent()&&session.get().getTeacherProfile().getId().equals(teacher.get().getId())){

            if(!ownership(authentication,teacher.get().getUser().getUsername()))return "Teacher not matched ";

            if(!session.get().getStatus().equals("PENDING")){
                return "ALREADY PROCESSED";
            }

            session.get().setStatus("REJECTED");
            sessionRequestRepository.save(session.get());
            return "REJECTED";
        }
        return "Session or Teacher not found ";
    }

    public boolean deleteSession( Long id,Authentication authentication){

        Optional<SessionRequest> session = sessionRequestRepository.findById(id);

        if(session.isPresent()){
            StudentProfile studentProfile = session.get().getStudentProfile();

            if(!ownership(authentication,studentProfile.getUser().getUsername()))return false;
            sessionRequestRepository.delete(session.get());

            return true;

        }

        return false;

    }

    public String getStatus(Long id, Authentication authentication){

        Optional<SessionRequest> sessionRequest = sessionRequestRepository.findById(id);

        if(sessionRequest.isPresent()){
            String studentUsername = sessionRequest.get().getStudentProfile().getUser().getUsername();
            String teacherUsername = sessionRequest.get().getTeacherProfile().getUser().getUsername();

            if(!ownership(authentication, studentUsername) && !ownership(authentication, teacherUsername))
                return "Some mismatch in request occurred ";

            return sessionRequest.get().getStatus();
        }
        return "NOT FOUND";
    }
    public Boolean updateSession(Long id, UpdateSessionDTO dto,Authentication authentication){
        Optional<SessionRequest> oldRequest = sessionRequestRepository.findById(id);
        if(oldRequest.isPresent()){

            if(!ownership(authentication,oldRequest.get().getStudentProfile().getUser().getUsername()))
                return false;

            oldRequest.get().setSubject(dto.getSubject());
            oldRequest.get().setDescription(dto.getDescription());
            sessionRequestRepository.save(oldRequest.get());
            return true;
        }
        return false;

    }


}
