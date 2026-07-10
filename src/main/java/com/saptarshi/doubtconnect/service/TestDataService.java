package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.StudentDto;
import com.saptarshi.doubtconnect.dto.TeacherDto;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.repository.ReportRepository;
import com.saptarshi.doubtconnect.repository.StudentProfileRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TestDataService {
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;

    public TestDataService(TeacherProfileRepository teacherProfileRepository,
                           StudentProfileRepository studentProfileRepository) {
        this.teacherProfileRepository = teacherProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    public List<TeacherDto> getTestTeachers() {
        return teacherProfileRepository.findAll().stream()
                .filter(t -> t.getUser() != null
                        && t.getUser().getUsername() != null
                        && t.getUser().getUsername().startsWith("test_educator"))
                .map(this::toTeacherDto)
                .collect(Collectors.toList());
    }

    public List<StudentDto> getTestStudents() {
        return studentProfileRepository.findAll().stream()
                .filter(s -> s.getUser() != null
                        && s.getUser().getUsername() != null
                        && s.getUser().getUsername().startsWith("test_student"))
                .map(this::toStudentDto)
                .collect(Collectors.toList());
    }

    private TeacherDto toTeacherDto(TeacherProfile teacher) {
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
        dto.setPaymentMethod(
                teacher.getPayoutDetails() != null
                        ? teacher.getPayoutDetails().getAccountStatus()
                        : "N/A"
        );
        return dto;
    }

    private StudentDto toStudentDto(StudentProfile student) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setName(student.getUser().getUsername());
        dto.setProfilePictureUrl(student.getProfilePictureUrl());
        dto.setGrade(student.getGrade());
        dto.setBoard(student.getBoard());
        dto.setLanguage(student.getLanguage());
        return dto;
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void remove_reports() {

        reportRepository.findAll().stream()

                .filter(report ->
                        report.getStudentProfile().getUser().getUsername().startsWith("test_student")
                                || report.getTeacherProfile().getUser().getUsername().startsWith("test_educator"))

                .forEach(reportRepository::delete);

    }

    private boolean isAdmin(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent() && "ADMIN".equals(user.get().getRole());
    }

    public List<TeacherDto> getAllTestTeachers(Authentication authentication) {

        boolean admin = isAdmin(authentication.getName());

        Optional<StudentProfile> student = admin
                ? Optional.empty()
                : studentProfileRepository.findByUserUsername(authentication.getName());

        if (!admin && student.isEmpty()) {
            return new ArrayList<>();
        }

        return teacherProfileRepository.findAll()
                .stream()

                .filter(teacher ->
                        teacher.getUser() != null
                                && teacher.getUser().getUsername() != null
                                && teacher.getUser().getUsername().startsWith("test_educator"))

                .filter(TeacherProfile::isActive)

                .filter(teacher ->
                        admin || reportRepository.findByStudentProfileAndTeacherProfile(
                                student.get(),
                                teacher
                        ).isEmpty())

                .filter(teacher ->
                        teacher.getPayoutDetails() != null
                                && "ACTIVE".equals(
                                teacher.getPayoutDetails().getAccountStatus()))

                .map(this::toTeacherDto)

                .collect(Collectors.toList());
    }

    public List<TeacherDto> searchTestTeachers(
            String subject,
            Authentication authentication) {

        boolean admin = isAdmin(authentication.getName());

        Optional<StudentProfile> student = admin
                ? Optional.empty()
                : studentProfileRepository.findByUserUsername(authentication.getName());
//        System.out.println("Authentication user = " + authentication.getName());
//        System.out.println("Is admin = " + admin);
//        System.out.println("Student found = " + student.isPresent());

        if (!admin && student.isEmpty()) {
            return new ArrayList<>();
        }

        String searchSubject = subject.trim().toLowerCase();


        return teacherProfileRepository.findAll()
                .stream()

                .filter(teacher ->
                        teacher.getUser() != null
                                && teacher.getUser().getUsername() != null
                                && teacher.getUser().getUsername().startsWith("test_educator"))

                .filter(TeacherProfile::isActive)

                .filter(teacher ->
                        admin || reportRepository.findByStudentProfileAndTeacherProfile(
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

                .map(this::toTeacherDto)

                .collect(Collectors.toList());
    }

    public List<TeacherDto> getFavouriteTestTeachers(Long studentId) {

        Optional<StudentProfile> student =
                studentProfileRepository.findById(studentId);

        if (student.isEmpty()) {
            return new ArrayList<>();
        }

        return student.get()
                .getFavourites()
                .stream()

                .filter(teacher ->
                        teacher.getUser() != null
                                && teacher.getUser().getUsername() != null
                                && teacher.getUser().getUsername().startsWith("test_educator"))

                .filter(TeacherProfile::isActive)

                .filter(teacher ->
                        teacher.getPayoutDetails() != null
                                && "ACTIVE".equals(
                                teacher.getPayoutDetails().getAccountStatus()))

                .map(this::toTeacherDto)

                .collect(Collectors.toList());
    }
}
