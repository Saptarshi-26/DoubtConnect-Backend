package com.saptarshi.doubtconnect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.saptarshi.doubtconnect.dto.*;
import com.saptarshi.doubtconnect.entity.*;
import com.saptarshi.doubtconnect.google.EmailService;
import com.saptarshi.doubtconnect.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private SessionRequestImageRepository sessionRequestImageRepository;

    @Autowired
    private Cloudinary cloudinary;


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
        dto.setImages(
                sessionRequest.getImages()
                        .stream()
                        .map(image -> {
                            SessionRequestImageDto imageDto =
                                    new SessionRequestImageDto();

                            imageDto.setId(image.getId());
                            imageDto.setImageUrl(image.getImageUrl());

                            return imageDto;
                        })
                        .toList()
        );

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
    public boolean sendRequest(
            SessionRequestDTO dto,
            MultipartFile[] images,
            Authentication authentication) {

        if (dto.getStudentProfileId() == null || dto.getTeacherProfileId() == null) {
            return false;
        }

        Optional<StudentProfile> student =
                studentProfileRepository.findById(dto.getStudentProfileId());

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(dto.getTeacherProfileId());

        if (student.isEmpty() || teacher.isEmpty()) {
            return false;
        }

        if (!ownership(authentication,
                student.get().getUser().getUsername())) {
            return false;
        }

        if (dto.getSubject() == null || dto.getSubject().trim().isBlank()) {
            return false;
        }

        if (dto.getDescription() == null || dto.getDescription().trim().isBlank()) {
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
        request.setTotalAmount(
                (dto.getSessionDuration() / 30.0)
                        * teacher.get().getRatePerThirtyMin());

        sessionRequestRepository.save(request);
        if (images != null) {

            if (images.length > 5) {
                return false;
            }

            for (MultipartFile file : images) {

                if (file == null || file.isEmpty()) {
                    continue;
                }

                if (file.getSize() > 5 * 1024 * 1024) {
                    return false;
                }

                String contentType = file.getContentType();

                if (contentType == null ||
                        !(contentType.equals("image/jpeg")
                                || contentType.equals("image/png")
                                || contentType.equals("image/webp"))) {

                    return false;
                }
            }
        }

        if (images != null) {

            for (MultipartFile file : images) {

                if (file == null || file.isEmpty()) {
                    continue;
                }

                try {

                    Map<?, ?> result =
                            cloudinary.uploader().upload(
                                    file.getBytes(),
                                    ObjectUtils.emptyMap());

                    SessionRequestImage image =
                            new SessionRequestImage();

                    image.setImageUrl(
                            result.get("secure_url").toString());

                    image.setPublicId(
                            result.get("public_id").toString());

                    image.setSessionRequest(request);

                    sessionRequestImageRepository.save(image);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return true;
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
                        dto.setImages(
                                request.getImages()
                                        .stream()
                                        .map(image -> {
                                            SessionRequestImageDto imageDto =
                                                    new SessionRequestImageDto();

                                            imageDto.setId(image.getId());
                                            imageDto.setImageUrl(image.getImageUrl());

                                            return imageDto;
                                        })
                                        .toList()
                        );

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
                        dto.setImages(
                                request.getImages()
                                        .stream()
                                        .map(image -> {
                                            SessionRequestImageDto imageDto =
                                                    new SessionRequestImageDto();

                                            imageDto.setId(image.getId());
                                            imageDto.setImageUrl(image.getImageUrl());

                                            return imageDto;
                                        })
                                        .toList()
                        );

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

            List<TeacherAvailability> bookedSlots =
                    teacherAvailabilityRepository
                            .findByTeacherProfileAndBookedTrueOrderByStartTimeAsc(teacher);

            for (TeacherAvailability slot : bookedSlots) {
                if (!slot.getStartTime().isBefore(event.get().getStartTime())
                        && !slot.getEndTime().isAfter(event.get().getEndTime())) {
                    slot.setBooked(false);
                    slot.setAvailable(true);
                }
            }

            teacherAvailabilityRepository.saveAll(bookedSlots);

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
