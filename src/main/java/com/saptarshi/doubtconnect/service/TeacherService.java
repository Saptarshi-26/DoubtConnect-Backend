package com.saptarshi.doubtconnect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.saptarshi.doubtconnect.dto.*;
import com.saptarshi.doubtconnect.entity.*;
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
    private TeacherMeetingDetailsRepository teacherMeetingDetailsRepository;

    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PaymentOutRepository paymentOutRepository;


    private String extractPublicId(String url) {
        if (url == null || url.isBlank()) return null;

        try {
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) return null;

            String afterUpload = url.substring(uploadIndex + "/upload/".length());

            // strip version segment like v1234567890/
            if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }

            // strip file extension
            int dotIndex = afterUpload.lastIndexOf(".");
            return dotIndex != -1 ? afterUpload.substring(0, dotIndex) : afterUpload;

        } catch (Exception e) {
            return null;
        }
    }


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

        String searchSubject = subject.trim().toLowerCase();

        return teacherProfileRepository.findAll()
                .stream()
                .filter(TeacherProfile::isActive)

                .filter(teacher ->
                        reportRepository.findByStudentProfileAndTeacherProfile(
                                student.get(),
                                teacher
                        ).isEmpty())

                .filter(teacher ->
                        teacher.getPayoutDetails() != null
                                && "ACTIVE".equals(
                                teacher.getPayoutDetails().getAccountStatus()))

                .filter(teacher ->
                        teacher.getSubjects()
                                .stream()
                                .anyMatch(x ->
                                        x.toLowerCase()
                                                .contains(searchSubject)))

                .map(teacher -> {

                    TeacherDto dto = new TeacherDto();

                    dto.setId(teacher.getId());
                    dto.setName(teacher.getUser().getDisplayName());
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

    public List<TeacherDto> findAll(Authentication authentication) {

        long total = System.currentTimeMillis();

        long t1 = System.currentTimeMillis();
        Optional<StudentProfile> student =
                studentProfileRepository.findByUserUsername(authentication.getName());

        System.out.println("Student lookup = "
                + (System.currentTimeMillis() - t1) + " ms");

        if (student.isEmpty()) {
            return new ArrayList<>();
        }

        long t2 = System.currentTimeMillis();

        List<TeacherProfile> teachers = teacherProfileRepository.findAll();

        System.out.println("teacherProfileRepository.findAll = "
                + (System.currentTimeMillis() - t2) + " ms");

        long t3 = System.currentTimeMillis();

        List<TeacherDto> result = teachers.stream()

                .filter(teacher ->
                        reportRepository.findByStudentProfileAndTeacherProfile(
                                student.get(),
                                teacher
                        ).isEmpty())

                .filter(TeacherProfile::isActive)

                .filter(x -> x.getPayoutDetails() != null
                        && "ACTIVE".equals(
                        x.getPayoutDetails().getAccountStatus()))

                .map(teacher -> {

                    TeacherDto dto = new TeacherDto();

                    dto.setId(teacher.getId());
                    dto.setName(teacher.getUser().getDisplayName());
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

        System.out.println("Stream processing = "
                + (System.currentTimeMillis() - t3) + " ms");

        System.out.println("TeacherService TOTAL = "
                + (System.currentTimeMillis() - total) + " ms");

        return result;
    }

    @Transactional
    public TeacherDto uploadProfilePicture(
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

        if (!teacher.get().isActive()) {
            return null;
        }

        Map<?, ?> result =
                cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.emptyMap());

        String imageUrl =
                result.get("secure_url").toString();

        teacher.get().setProfilePictureUrl(imageUrl);

        teacherProfileRepository.save(teacher.get());

        TeacherDto dto = new TeacherDto();

        dto.setId(teacher.get().getId());
        dto.setName(teacher.get().getUser().getDisplayName());
        dto.setProfilePictureUrl(teacher.get().getProfilePictureUrl());
        dto.setSubjects(teacher.get().getSubjects());
        dto.setLanguage(teacher.get().getLanguage());
        dto.setBio(teacher.get().getBio());
        dto.setRatePerThirtyMin(teacher.get().getRatePerThirtyMin());
        dto.setRating(teacher.get().getRating());
        dto.setNumberOfRatings(teacher.get().getNumberOfRatings());

        if (teacher.get().getPayoutDetails().getUpiDetails() != null) {
            dto.setPaymentMethod("UPI");
        } else {
            dto.setPaymentMethod("BANK");
        }

        return dto;

    }


    public List<TeacherDto> findAllInternal() {
        return teacherProfileRepository.findAll()
                .stream()
                .filter(TeacherProfile::isActive)
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


    public Optional<TeacherDto> findTeacher(Long id) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(id);

        if (teacher.isEmpty()) {
            return Optional.empty();
        }

        if (!teacher.get().isActive()) {
            return Optional.empty();
        }

        TeacherDto dto = new TeacherDto();

        dto.setId(teacher.get().getId());
        dto.setName(teacher.get().getUser().getDisplayName());
        dto.setProfilePictureUrl(teacher.get().getProfilePictureUrl());
        dto.setSubjects(teacher.get().getSubjects());
        dto.setLanguage(teacher.get().getLanguage());
        dto.setBio(teacher.get().getBio());
        dto.setRatePerThirtyMin(teacher.get().getRatePerThirtyMin());
        dto.setRating(teacher.get().getRating());
        dto.setNumberOfRatings(teacher.get().getNumberOfRatings());

        if (teacher.get().getPayoutDetails() != null
                && teacher.get().getPayoutDetails().getUpiDetails() != null) {

            dto.setPaymentMethod("UPI");

        } else {

            dto.setPaymentMethod("BANK");
        }

        return Optional.of(dto);
    }

    @Transactional
    public TeacherMeetingDetails saveMeetingDetails(
            Long teacherProfileId,
            TeacherMeetingDetailsDto dto,
            Authentication authentication) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return null;
        }

        if (!isOwner(teacher.get(), authentication.getName())) {
            return null;
        }

        if (!teacher.get().isActive()) {
            return null;
        }

        TeacherMeetingDetails details =
                teacherMeetingDetailsRepository
                        .findByTeacherProfile(teacher.get())
                        .orElse(new TeacherMeetingDetails());

        details.setTeacherProfile(teacher.get());
        details.setMeetingPlatform(dto.getMeetingPlatform());
        details.setMeetingLink(dto.getMeetingLink());

        return teacherMeetingDetailsRepository.save(details);
    }

    public TeacherMeetingDetailsDto getMeetingDetails(
            Long teacherProfileId,
            Authentication authentication) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return null;
        }

        if (!isOwner(teacher.get(), authentication.getName())) {
            return null;
        }

        if (!teacher.get().isActive()) {
            return null;
        }

        Optional<TeacherMeetingDetails> details =
                teacherMeetingDetailsRepository
                        .findByTeacherProfile(teacher.get());

        if (details.isEmpty()) {
            return null;
        }

        TeacherMeetingDetailsDto dto =
                new TeacherMeetingDetailsDto();

        dto.setMeetingPlatform(
                details.get().getMeetingPlatform());

        dto.setMeetingLink(
                details.get().getMeetingLink());

        return dto;
    }


    public boolean updateBio(Long id, UpdateBioDTO dto, String username) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(id);

        if (teacher.isEmpty() || !isOwner(teacher.get(), username)) {
            return false;
        }

        if (!teacher.get().isActive()) {
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

        if (!teacher.get().isActive()) {
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

        if (!teacher.get().isActive()) {
            return false;
        }

        if (!teacher.get().getSubjects().contains(dto.getSubject().trim())) {

            teacher.get().getSubjects().add(dto.getSubject().trim());

            teacherProfileRepository.save(teacher.get());
        }

        return true;
    }

    public boolean removeSubject(Long id, SubjectDTO dto, String username) {

        Optional<TeacherProfile> teacher = teacherProfileRepository.findById(id);

        if (teacher.isEmpty() || !isOwner(teacher.get(), username)) {
            return false;
        }

        if (!teacher.get().isActive()) {
            return false;
        }

        teacher.get().getSubjects().remove(dto.getSubject());

        teacherProfileRepository.save(teacher.get());

        return true;
    }

    @Transactional
    public boolean deleteTeacher(Long teacherId, Authentication authentication) {

        Optional<TeacherProfile> teacherOpt =
                teacherProfileRepository.findById(teacherId);

        if (teacherOpt.isEmpty()) {
            return false;
        }

        TeacherProfile teacher = teacherOpt.get();

        if (!isOwner(teacher, authentication.getName())) {
            return false;
        }

        if (!teacher.isActive()) {
            return false;
        }

        if (authentication.getName().startsWith("test_educator")) return false;

        // Cancel all pending session requests
        List<SessionRequest> pendingRequests =
                sessionRequestRepository.findByTeacherProfileAndStatus(teacher, "PENDING");
        for (SessionRequest request : pendingRequests) {
            request.setStatus("CANCELLED");
        }
        sessionRequestRepository.saveAll(pendingRequests);

        // Cancel all upcoming session events
        List<SessionEvent> upcomingSessions =
                sessionEventRepository.findByTeacherProfileAndEventStatus(teacher, "UPCOMING");
        for (SessionEvent event : upcomingSessions) {
            event.setEventStatus("CANCELLED");
            event.getSessionRequest().setStatus("CANCELLED");
            sessionRequestRepository.save(event.getSessionRequest());
        }
        sessionEventRepository.saveAll(upcomingSessions);

        // Hard-delete availability slots
        teacherAvailabilityRepository.deleteAllByTeacherProfile(teacher);

        // Hard-delete meeting/Google credentials
        teacherMeetingDetailsRepository.findByTeacherProfile(teacher)
                .ifPresent(teacherMeetingDetailsRepository::delete);

        // Hard-delete payout details
        if (teacher.getPayoutDetails() != null) {
            paymentOutRepository.delete(teacher.getPayoutDetails());
            teacher.setPayoutDetails(null);
        }

        // Remove from all students' favourites
        studentProfileRepository.removeFromAllFavourites(teacherId);

        // Delete profile picture from Cloudinary (only if it's a real Cloudinary URL)
        String publicId = extractPublicId(teacher.getProfilePictureUrl());
        if (publicId != null) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (IOException e) {
                System.err.println("Failed to delete Cloudinary image: " + e.getMessage());
            }
        }

        // Scrub every personal/profile field
        teacher.setProfilePictureUrl(null);
        teacher.setBio("[deleted]");
        teacher.setSubjects(new ArrayList<>());
        teacher.setLanguage("[deleted]");
        teacher.setRatePerThirtyMin(0);
        teacher.setGoogleEmail("deleted_" + teacher.getId() + "@deleted.doubtconnect.internal");
        teacher.setActive(false);
        teacherProfileRepository.save(teacher);

        User user = teacher.getUser();
        user.setUsername("deleted_teacher_" + user.getId());
        user.setDisplayName("Teacher not found ");
        userRepository.save(user);

        return true;
    }
}