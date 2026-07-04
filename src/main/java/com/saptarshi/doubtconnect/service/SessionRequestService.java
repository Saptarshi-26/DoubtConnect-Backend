package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.SessionActionDTO;
import com.saptarshi.doubtconnect.dto.SessionRequestDTO;
import com.saptarshi.doubtconnect.dto.SessionRequestResponseDto;
import com.saptarshi.doubtconnect.dto.UpdateSessionDTO;
import com.saptarshi.doubtconnect.entity.*;
import com.saptarshi.doubtconnect.google.EmailService;
import com.saptarshi.doubtconnect.repository.*;
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
    private UserRepository userRepository ;

    @Autowired
    private SessionRequestRepository sessionRequestRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;


    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Autowired
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

    @Autowired
    private EmailService emailService;


    private boolean ownership(Authentication authentication, String username){
        Optional<User> user = userRepository.findByUsername(authentication.getName());

        return user.filter(value -> authentication.getName().equals(username) || value.getRole().equals("ADMIN")).isPresent();

    }

    public Optional<SessionRequestResponseDto> getSessionRequestById(
            Long id,
            Authentication authentication) {

        Optional<SessionRequest> request =
                sessionRequestRepository.findById(id);

        if (request.isEmpty()) {
            return Optional.empty();
        }

        SessionRequest sessionRequest = request.get();

        boolean studentOwner =
                ownership(
                        authentication,
                        sessionRequest.getStudentProfile()
                                .getUser()
                                .getUsername());

        boolean teacherOwner =
                ownership(
                        authentication,
                        sessionRequest.getTeacherProfile()
                                .getUser()
                                .getUsername());

        if (!studentOwner && !teacherOwner) {
            return Optional.empty();
        }

        SessionRequestResponseDto dto =
                new SessionRequestResponseDto();

        dto.setId(sessionRequest.getId());
        dto.setSubject(sessionRequest.getSubject());
        dto.setDescription(sessionRequest.getDescription());
        dto.setStatus(sessionRequest.getStatus());
        dto.setSessionDuration(sessionRequest.getSessionDuration());
        dto.setTotalAmount(sessionRequest.getTotalAmount());

        dto.setStudentId(
                sessionRequest.getStudentProfile().getId());

        dto.setStudentName(
                sessionRequest.getStudentProfile()
                        .getUser()
                        .getUsername());

        dto.setStudentProfilePictureUrl(
                sessionRequest.getStudentProfile()
                        .getProfilePictureUrl());

        dto.setTeacherId(
                sessionRequest.getTeacherProfile().getId());

        dto.setTeacherName(
                sessionRequest.getTeacherProfile()
                        .getUser()
                        .getUsername());

        dto.setTeacherProfilePictureUrl(
                sessionRequest.getTeacherProfile()
                        .getProfilePictureUrl());

        return Optional.of(dto);
    }

    @Transactional
    public boolean sendRequest(SessionRequestDTO dto, Authentication authentication){
//        System.out.println("Student ID = " + dto.getStudentProfileId());
//        System.out.println("Teacher ID = " + dto.getTeacherProfileId());
        if (dto.getStudentProfileId() == null || dto.getTeacherProfileId() == null) {
            return false;
        }
        Optional<StudentProfile> student =
                studentProfileRepository.findById(dto.getStudentProfileId());


        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(dto.getTeacherProfileId());


        if(student.isPresent() && teacher.isPresent()) {

            if (!ownership(authentication,
                    student.get().getUser().getUsername())) {
                return false;
            }

            if (dto.getSubject() == null ||
                    dto.getSubject().trim().isBlank()) {
                return false;
            }

            if (dto.getDescription() == null ||
                    dto.getDescription().trim().isBlank()) {
                return false;
            }

            if (dto.getSubject().trim().length() > 100) {
                return false;
            }

            if (dto.getDescription().trim().length() > 1000) {
                return false;
            }

            if (dto.getSessionDuration() != 30 &&
                    dto.getSessionDuration() != 60 &&
                    dto.getSessionDuration() != 90 &&
                    dto.getSessionDuration() != 120) {
                return false;
            }

            if (sessionRequestRepository
                    .existsByStudentProfileAndTeacherProfileAndDescriptionAndStatus(
                            student.get(),
                            teacher.get(),
                            dto.getDescription().trim(),
                            "PENDING")) {

                return false;
            }

            SessionRequest request = new SessionRequest();


            request.setStatus("PENDING");
            request.setSubject(dto.getSubject().trim());
            request.setDescription(dto.getDescription().trim());
            request.setStudentProfile(student.get());
            request.setTeacherProfile(teacher.get());
            request.setSessionDuration(dto.getSessionDuration());
            request.setTotalAmount((dto.getSessionDuration()/30.0)*teacher.get().getRatePerThirtyMin());


            sessionRequestRepository.save(request);

            return true;
        }

        return false;
    }

    public List<SessionRequestResponseDto> findByStudentProfile(Long id,Authentication authentication) {

        Optional<StudentProfile> studentProfile =
                studentProfileRepository.findById(id);

        if (studentProfile.isPresent()) {

            if (!ownership(authentication,
                    studentProfile.get().getUser().getUsername())) {
                return new ArrayList<>();
            }

            return sessionRequestRepository
                    .findByStudentProfile(studentProfile.get())
                    .stream()
                    .map(request -> {

                        SessionRequestResponseDto dto = new SessionRequestResponseDto();

                        dto.setId(request.getId());
                        dto.setSubject(request.getSubject());
                        dto.setDescription(request.getDescription());
                        dto.setStatus(request.getStatus());
                        dto.setSessionDuration(request.getSessionDuration());
                        dto.setTotalAmount(request.getTotalAmount());

                        dto.setStudentId(
                                request.getStudentProfile().getId());

                        dto.setStudentName(
                                request.getStudentProfile().getUser().getUsername());

                        dto.setStudentProfilePictureUrl(
                                request.getStudentProfile().getProfilePictureUrl());

                        dto.setTeacherId(
                                request.getTeacherProfile().getId());

                        dto.setTeacherName(
                                request.getTeacherProfile().getUser().getUsername());

                        dto.setTeacherProfilePictureUrl(
                                request.getTeacherProfile().getProfilePictureUrl());

                        return dto;

                    }).toList();
        }

        return new ArrayList<>();
    }


    public List<SessionRequestResponseDto> findByTeacherProfile(
            Long id,
            Authentication authentication) {

        Optional<TeacherProfile> teacherProfile =
                teacherProfileRepository.findById(id);

        if (teacherProfile.isPresent()) {

            if (!ownership(authentication,
                    teacherProfile.get().getUser().getUsername())) {
                return new ArrayList<>();
            }

            return sessionRequestRepository
                    .findByTeacherProfile(teacherProfile.get())
                    .stream()
                    .map(request -> {

                        SessionRequestResponseDto dto = new SessionRequestResponseDto();

                        dto.setId(request.getId());
                        dto.setSubject(request.getSubject());
                        dto.setDescription(request.getDescription());
                        dto.setStatus(request.getStatus());
                        dto.setSessionDuration(request.getSessionDuration());
                        dto.setTotalAmount(request.getTotalAmount());

                        dto.setStudentId(
                                request.getStudentProfile().getId());

                        dto.setStudentName(
                                request.getStudentProfile().getUser().getUsername());

                        dto.setStudentProfilePictureUrl(
                                request.getStudentProfile().getProfilePictureUrl());

                        dto.setTeacherId(
                                request.getTeacherProfile().getId());

                        dto.setTeacherName(
                                request.getTeacherProfile().getUser().getUsername());

                        dto.setTeacherProfilePictureUrl(
                                request.getTeacherProfile().getProfilePictureUrl());

                        return dto;

                    }).toList();
        }

        return new ArrayList<>();
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
            emailService.sendSessionAcceptedEmail(
                    session.get().getStudentProfile().getGoogleEmail(),
                    session.get().getStudentProfile().getUser().getUsername()
            );
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
            emailService.sendSessionRejectedEmail(
                    session.get().getStudentProfile().getGoogleEmail(),
                    session.get().getStudentProfile().getUser().getUsername()
            );
            return "REJECTED";
        }
        return "Session or Teacher not found ";
    }

    @Transactional
    public boolean deleteSession(Long id,
                                 Authentication authentication) {

        Optional<SessionRequest> session =
                sessionRequestRepository.findById(id);

        if (session.isEmpty()) {
            return false;
        }

        if (!ownership(authentication,
                session.get().getStudentProfile()
                        .getUser().getUsername())) {

            return false;
        }

        Optional<SessionEvent> event =
                sessionEventRepository.findBySessionRequest(session.get());

        if (event.isPresent()) {
            if (!"UPCOMING".equals(event.get().getEventStatus())) {
                return false;
            }

            TeacherProfile teacher = event.get().getTeacherProfile();

            Optional<TeacherAvailability> slot =
                    teacherAvailabilityRepository
                            .findByTeacherProfileAndStartTimeAndEndTime(
                                    teacher,
                                    event.get().getStartTime(),
                                    event.get().getEndTime());

            if (slot.isPresent()) {

                slot.get().setBooked(false);
                slot.get().setAvailable(true);

                teacherAvailabilityRepository.save(slot.get());
            }

            sessionEventRepository.delete(event.get());
        }

        sessionRequestRepository.delete(session.get());

        return true;
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
