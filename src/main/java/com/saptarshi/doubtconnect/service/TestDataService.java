package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.StudentDto;
import com.saptarshi.doubtconnect.dto.TeacherDto;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.repository.ReportRepository;
import com.saptarshi.doubtconnect.repository.StudentProfileRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestDataService {
    @Autowired
    private ReportRepository reportRepository;

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
}