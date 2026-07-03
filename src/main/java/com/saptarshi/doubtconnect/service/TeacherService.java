package com.saptarshi.doubtconnect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.saptarshi.doubtconnect.dto.*;
import com.saptarshi.doubtconnect.entity.*;
import com.saptarshi.doubtconnect.google.GoogleCredentialRepository;
import com.saptarshi.doubtconnect.payment.repository.PaymentOutRepository;
import com.saptarshi.doubtconnect.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TeacherService {

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRequestRepository sessionRequestRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

    @Autowired
    private GoogleCredentialRepository googleCredentialRepository;

    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private PaymentOutRepository paymentOutRepository;

    private boolean isOwner(TeacherProfile teacher, String username) {

        Optional<User> user = userRepository.findByUsername(username);

        return teacher.getUser().getUsername().equals(username) || user.isPresent() && user.get().getRole().equals("ADMIN");
    }


    public List<TeacherDto> searchBySubject(String subject, Authentication authentication) {
        Optional<StudentProfile> student =
                studentProfileRepository.findByUserUsername(
                        authentication.getName());

        if (student.isEmpty()) {
            return new ArrayList<>();
        }

        String searchSubject = subject.trim().toUpperCase();

        return teacherProfileRepository.findAll()
                .stream()
                .filter(teacher ->
                        teacher.getPayoutDetails() != null &&
                                "ACTIVE".equals(teacher.getPayoutDetails().getAccountStatus()))
                .filter(teacher ->
                        teacher.getSubjects()
                                .stream()
                                .map(String::toUpperCase)
                                .anyMatch(s -> s.equals(searchSubject)))
                .map(teacher -> {

                    TeacherDto dto = new TeacherDto();

                    dto.setId(teacher.getId());
                    dto.setName(teacher.getUser().getUsername());
                    dto.setProfilePictureUrl(teacher.getProfilePictureUrl());
                    dto.setSubjects(teacher.getSubjects());
                    dto.setLanguage(teacher.getLanguage());
                    dto.setBio(teacher.getBio());
                    dto.setRatePerThirtyMin(teacher.getRatePerThirtyMin());
                    dto.setRating(teacher.getRating());
                    dto.setNumberOfRatings(teacher.getNumberOfRatings());

                    if (teacher.getPayoutDetails().getUpiDetails() != null) {
                        dto.setPaymentMethod("UPI");
                    } else {
                        dto.setPaymentMethod("BANK");
                    }

                    return dto;
                })
                .toList();
    }

    @Transactional
    public TeacherProfile uploadProfilePicture(
            Long teacherProfileId,
            MultipartFile file,
            Authentication authentication) throws IOException {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return null;
        }

        if (!isOwner(teacher.get(), authentication.getName())) {
            return null;
        }

        Map<?, ?> result =
                cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.emptyMap());

        String imageUrl =
                result.get("secure_url").toString();

        teacher.get().setProfilePictureUrl(imageUrl);

        return teacherProfileRepository.save(teacher.get());
    }


   public List<TeacherDto> findAllInternal(){
//       System.out.println("ADMIN GET ALL INTERNAL");
//       System.out.println(teacherProfileRepository.findAll().size());
       return teacherProfileRepository.findAll()
               .stream()
               .map(teacher -> {

                   TeacherDto dto = new TeacherDto();

                   dto.setId(teacher.getId());
                   dto.setName(teacher.getUser().getUsername());
                   dto.setProfilePictureUrl(teacher.getProfilePictureUrl());
                   dto.setSubjects(teacher.getSubjects());
                   dto.setLanguage(teacher.getLanguage());
                   dto.setBio(teacher.getBio());
                   dto.setRatePerThirtyMin(teacher.getRatePerThirtyMin());
                   dto.setRating(teacher.getRating());
                   dto.setNumberOfRatings(teacher.getNumberOfRatings());

                   if (teacher.getPayoutDetails() != null) {

                       if (teacher.getPayoutDetails().getUpiDetails() != null) {
                           dto.setPaymentMethod("UPI");
                       } else if (teacher.getPayoutDetails().getBankDetails() != null) {
                           dto.setPaymentMethod("BANK");
                       }
                   }

                   return dto;

               }).toList();
   }

    public List<TeacherDto> findAll() {

        return teacherProfileRepository.findAll()
                .stream()
                .filter(x -> x.getPayoutDetails() != null
                        && "ACTIVE".equals(x.getPayoutDetails().getAccountStatus()))
                .map(teacher -> {

                    TeacherDto dto = new TeacherDto();

                    dto.setId(teacher.getId());
                    dto.setName(teacher.getUser().getUsername());
                    dto.setProfilePictureUrl(teacher.getProfilePictureUrl());
                    dto.setSubjects(teacher.getSubjects());
                    dto.setLanguage(teacher.getLanguage());
                    dto.setBio(teacher.getBio());
                    dto.setRatePerThirtyMin(teacher.getRatePerThirtyMin());
                    dto.setRating(teacher.getRating());
                    dto.setNumberOfRatings(teacher.getNumberOfRatings());

                    if (teacher.getPayoutDetails().getUpiDetails() != null) {
                        dto.setPaymentMethod("UPI");
                    } else {
                        dto.setPaymentMethod("BANK");
                    }

                    return dto;

                }).toList();
    }

    public Optional<TeacherDto> findTeacher(Long id) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(id);

        if (teacher.isEmpty()) {
            return Optional.empty();
        }

        TeacherDto dto = new TeacherDto();

        dto.setId(teacher.get().getId());
        dto.setName(teacher.get().getUser().getUsername());
        dto.setProfilePictureUrl(teacher.get().getProfilePictureUrl());
        dto.setSubjects(teacher.get().getSubjects());
        dto.setLanguage(teacher.get().getLanguage());
        dto.setBio(teacher.get().getBio());
        dto.setRatePerThirtyMin(teacher.get().getRatePerThirtyMin());
        dto.setRating(teacher.get().getRating());
        dto.setNumberOfRatings(teacher.get().getNumberOfRatings());

        if (teacher.get().getPayoutDetails() != null
                && teacher.get().getPayoutDetails().getUpiDetails()!= null) {

            dto.setPaymentMethod("UPI");

        } else {

            dto.setPaymentMethod("BANK");
        }

        return Optional.of(dto);
    }


    public boolean updateBio(Long id, UpdateBioDTO dto, String username) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(id);

        if (teacher.isEmpty() || !isOwner(teacher.get(), username)) {
            return false;
        }

        teacher.get().setBio(dto.getBio());
        teacherProfileRepository.save(teacher.get());

        return true;
    }

    public boolean updateRate(Long id, UpdateRateDTO dto, String username) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(id);

        if (teacher.isEmpty() || !isOwner(teacher.get(), username)) {
            return false;
        }

        teacher.get().setRatePerThirtyMin(dto.getRatePerThirtyMin());

        teacherProfileRepository.save(teacher.get());

        return true;
    }

    public boolean addSubject(Long id, SubjectDTO dto, String username) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(id);

        if (teacher.isEmpty() || !isOwner(teacher.get(), username)) {
            return false;
        }

        if (!teacher.get().getSubjects().contains(dto.getSubject())) {

            teacher.get().getSubjects().add(dto.getSubject());

            teacherProfileRepository.save(teacher.get());
        }

        return true;
    }

    public boolean removeSubject(Long id, SubjectDTO dto, String username) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(id);

        if (teacher.isEmpty() || !isOwner(teacher.get(), username)) {
            return false;
        }

        teacher.get().getSubjects().remove(dto.getSubject());

        teacherProfileRepository.save(teacher.get());

        return true;
    }

    @Transactional
    public boolean deleteTeacher(Long teacherId, Authentication authentication) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherId);

        if (teacher.isEmpty()) {
            return false;
        }

        if (!isOwner(
                teacher.get(),
                authentication.getName())) {
            return false;
        }

        // Cancel all pending session requests

        List<SessionRequest> pendingRequests =
                sessionRequestRepository
                        .findByTeacherProfileAndStatus(
                                teacher.get(),
                                "PENDING");

        for (SessionRequest request : pendingRequests) {
            request.setStatus("CANCELLED");
        }

        sessionRequestRepository.saveAll(pendingRequests);

        // Cancel all upcoming session events

        List<SessionEvent> upcomingSessions =
                sessionEventRepository
                        .findByTeacherProfileAndEventStatus(
                                teacher.get(),
                                "UPCOMING");

        for (SessionEvent event : upcomingSessions) {

            event.setEventStatus("CANCELLED");

            event.getSessionRequest()
                    .setStatus("CANCELLED");

            sessionRequestRepository.save(
                    event.getSessionRequest());
        }

        sessionEventRepository.saveAll(upcomingSessions);

        // Delete all availability slots

        teacherAvailabilityRepository.deleteAll(
                teacherAvailabilityRepository
                        .findByTeacherProfileOrderByStartTimeAsc(
                                teacher.get()));

        // Delete Google credentials

        googleCredentialRepository
                .findByTeacherProfile(teacher.get())
                .ifPresent(
                        googleCredentialRepository::delete);

        // Delete payout details (if present)

        if (teacher.get().getPayoutDetails() != null) {
            paymentOutRepository.delete(
                    teacher.get().getPayoutDetails());
        }

        User user = teacher.get().getUser();

        teacherProfileRepository.delete(teacher.get());

        userRepository.delete(user);

        return true;
    }
}