package com.saptarshi.doubtconnect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.saptarshi.doubtconnect.dto.FavouriteTeacherDTO;
import com.saptarshi.doubtconnect.dto.StudentDto;
import com.saptarshi.doubtconnect.dto.TeacherDto;
import com.saptarshi.doubtconnect.dto.UpdateStudentDto;
import com.saptarshi.doubtconnect.entity.*;
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
public class StudentService {

    @Autowired
    StudentProfileRepository studentProfileRepository;

    @Autowired
    TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private SessionRequestRepository sessionRequestRepository;

    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Autowired
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

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



    public List<StudentDto> getAll() {

        return studentProfileRepository.findAll()
                .stream().filter(StudentProfile::isActive)
                .map(student -> {

                    StudentDto dto = new StudentDto();

                    dto.setId(student.getId());
                    dto.setName(student.getUser().getDisplayName());
                    dto.setProfilePictureUrl(student.getProfilePictureUrl());
                    dto.setGrade(student.getGrade());
                    dto.setBoard(student.getBoard());
                    dto.setLanguage(student.getLanguage());

                    return dto;

                }).toList();
    }

    public Optional<StudentDto> findStudent(
            Long id,
            Authentication authentication) {

        Optional<StudentProfile> studentProfile =
                studentProfileRepository.findById(id);

        if((studentProfile.isEmpty())||
                (!isOwner(studentProfile.get(),authentication.getName()))&&
                (userRepository.findByUsername(authentication.getName()).isEmpty() ||
                !userRepository.findByUsername(authentication.getName()).get().getRole().
                        equals("TEACHER"))){
            return Optional.empty();
        }

//        if (studentProfile.isEmpty()){
//            if(!isOwner(studentProfile.get(), authentication.getName())) {
//
//                return Optional.empty();
//            }
//        }
        if(!studentProfile.get().isActive())return Optional.empty();

        StudentDto dto = new StudentDto();

        dto.setId(studentProfile.get().getId());
        dto.setName(studentProfile.get().getUser().getDisplayName());
        dto.setProfilePictureUrl(studentProfile.get().getProfilePictureUrl());
        dto.setGrade(studentProfile.get().getGrade());
        dto.setBoard(studentProfile.get().getBoard());
        dto.setLanguage(studentProfile.get().getLanguage());
        System.out.println(dto.getName());
        return Optional.of(dto);
    }

    private boolean isOwner(
            StudentProfile student,
            String username){

        Optional<User> user = userRepository.findByUsername(username);
        return student.getUser()
                .getUsername()
                .equals(username) ||
                user.isPresent() && user.get().getRole().equals("ADMIN");
    }



    public List<TeacherDto> getFavourites(
            Long id,
            String username){

        Optional<StudentProfile> student =
                studentProfileRepository.findById(id);

        if(student.isEmpty() ||
                !isOwner(student.get(), username)){
            return new ArrayList<>();
        }
        if(!student.get().isActive())return new ArrayList<>();
        return student.get().getFavourites().stream().map(teacher->{
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
    }

    public boolean addFavouriteTeacher(
            FavouriteTeacherDTO dto,
            String username){

        Optional<StudentProfile> student =
                studentProfileRepository
                        .findById(dto.getStudentId());

        Optional<TeacherProfile> teacher =
                teacherProfileRepository
                        .findById(dto.getTeacherId());

        if(student.isEmpty() ||
                teacher.isEmpty() ||
                !isOwner(student.get(), username)){
            return false;
        }
        if(!student.get().isActive())return false;

        if (!student.get().getFavourites().contains(teacher.get())) {

            student.get().getFavourites().add(teacher.get());

            teacherProfileRepository.save(teacher.get());
            studentProfileRepository.save(student.get());
        }

        return true;
    }

    public boolean removeFavouriteTeacher(
            FavouriteTeacherDTO dto,
            String username){

        Optional<StudentProfile> student =
                studentProfileRepository
                        .findById(dto.getStudentId());

        Optional<TeacherProfile> teacher =
                teacherProfileRepository
                        .findById(dto.getTeacherId());

        if(student.isEmpty() ||
                teacher.isEmpty() ||
                !isOwner(student.get(), username)){
            return false;
        }
        if(!student.get().isActive())return false;

        if (student.get().getFavourites().remove(teacher.get())) {

            teacherProfileRepository.save(teacher.get());
            studentProfileRepository.save(student.get());
        }
        return true;
    }

    @Transactional
    public StudentDto uploadProfilePicture(Long studentProfileId, MultipartFile file, Authentication authentication) throws IOException {

        Optional<StudentProfile> student =
                studentProfileRepository.findById(studentProfileId);

        if (student.isEmpty()) {
            return null;
        }
        if(!student.get().isActive())return null;

        if (!isOwner(student.get(), authentication.getName())) {
            return null;
        }

        Map<?, ?> result =
                cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder",
                                "student-profile-pictures"
                        ));

        String imageUrl =
                result.get("secure_url").toString();

        student.get().setProfilePictureUrl(imageUrl);

        studentProfileRepository.save(student.get());
        StudentDto dto = new StudentDto();

        dto.setId(student.get().getId());
        dto.setName(student.get().getUser().getDisplayName());
        dto.setProfilePictureUrl(student.get().getProfilePictureUrl());
        dto.setGrade(student.get().getGrade());
        dto.setBoard(student.get().getBoard());
        dto.setLanguage(student.get().getLanguage());
        return dto;



    }

    @Transactional
    public boolean deleteStudent(Long studentId, Authentication authentication) {

        Optional<StudentProfile> studentOpt =
                studentProfileRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return false;
        }

        StudentProfile student = studentOpt.get();

        if (!isOwner(student, authentication.getName())) {
            return false;
        }

        // Cancel pending requests
        List<SessionRequest> pendingRequests =
                sessionRequestRepository.findByStudentProfileAndStatus(student, "PENDING");
        for (SessionRequest request : pendingRequests) {
            request.setStatus("CANCELLED");
        }
        sessionRequestRepository.saveAll(pendingRequests);

        // Cancel upcoming sessions
        List<SessionEvent> upcomingSessions =
                sessionEventRepository.findByStudentProfileAndEventStatus(student, "UPCOMING");
        for (SessionEvent event : upcomingSessions) {
            event.setEventStatus("CANCELLED");
            event.getSessionRequest().setStatus("CANCELLED");

            List<TeacherAvailability> slots =
                    teacherAvailabilityRepository
                            .findByTeacherProfileAndBookedTrueOrderByStartTimeAsc(
                                    event.getTeacherProfile());

            for (TeacherAvailability slot : slots) {
                if (!slot.getStartTime().isBefore(event.getStartTime())
                        && !slot.getEndTime().isAfter(event.getEndTime())) {
                    slot.setBooked(false);
                    slot.setAvailable(true);
                }
            }
            teacherAvailabilityRepository.saveAll(slots);

            sessionRequestRepository.save(event.getSessionRequest());
        }
        sessionEventRepository.saveAll(upcomingSessions);

        // Remove this student's favourites list (their own picks of teachers)
        student.getFavourites().clear();
        studentProfileRepository.save(student);

        // Delete profile picture from Cloudinary (only if it's a real Cloudinary URL)
        String publicId = extractPublicId(student.getProfilePictureUrl());
        if (publicId != null) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (IOException e) {
                System.err.println("Failed to delete Cloudinary image: " + e.getMessage());
            }
        }

        // Scrub every personal/profile field
        student.setProfilePictureUrl(null);
        student.setGrade("[deleted]");
        student.setBoard("[deleted]");
        student.setLanguage("[deleted]");
        student.setGoogleEmail("deleted_" + student.getId() + "@deleted.doubtconnect.internal");
        student.setActive(false);
        studentProfileRepository.save(student);

        User user = student.getUser();
        user.setUsername("deleted_student_" + user.getId());
        user.setDisplayName("Student not found");
        userRepository.save(user);

        return true;
    }
    public boolean updateProfile(Long id, UpdateStudentDto dto, String username) {

        Optional<StudentProfile> student = studentProfileRepository.findById(id);

        if (student.isEmpty() || !isOwner(student.get(), username)) {
            return false;
        }
        if(!student.get().isActive())return false;
        if (dto.getGrade() != null && !dto.getGrade().isBlank()) {
            student.get().setGrade(dto.getGrade());
        }

        if (dto.getBoard() != null && !dto.getBoard().isBlank()) {
            student.get().setBoard(dto.getBoard());
        }

        if (dto.getLanguage() != null && !dto.getLanguage().isBlank()) {
            student.get().setLanguage(dto.getLanguage());
        }

        studentProfileRepository.save(student.get());

        return true;
    }
}