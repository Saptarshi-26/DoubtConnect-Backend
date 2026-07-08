package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.entity.Report;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.repository.ReportRepository;
import com.saptarshi.doubtconnect.repository.StudentProfileRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Transactional
    public boolean addReport(
            Long studentProfileId,
            Long teacherProfileId,
            String reason,
            String description,
            Authentication authentication) {

        Optional<StudentProfile> student =
                studentProfileRepository.findById(studentProfileId);

        if (student.isEmpty()) {
            return false;
        }

        if (!student.get().getUser().getUsername()
                .equals(authentication.getName())) {
            return false;
        }

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return false;
        }

        if (reportRepository.findByStudentProfileAndTeacherProfile(
                student.get(),
                teacher.get()).isPresent()) {

            return false;
        }

        Report report = new Report();

        report.setStudentProfile(student.get());
        report.setTeacherProfile(teacher.get());
        report.setReason(reason);
        report.setDescription(description);

        reportRepository.save(report);

        return true;
    }

    @Transactional
    public boolean removeReport(
            Long studentProfileId,
            Long teacherProfileId,
            Authentication authentication) {

        Optional<StudentProfile> student =
                studentProfileRepository.findById(studentProfileId);

        if (student.isEmpty()) {
            return false;
        }

        if (!student.get().getUser().getUsername()
                .equals(authentication.getName())) {
            return false;
        }

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return false;
        }

        Optional<Report> report =
                reportRepository.findByStudentProfileAndTeacherProfile(
                        student.get(),
                        teacher.get());

        if (report.isEmpty()) {
            return false;
        }

        reportRepository.delete(report.get());

        return true;
    }


    public List<Report> getReports(
            Long studentProfileId,
            Authentication authentication) {

        Optional<StudentProfile> student =
                studentProfileRepository.findById(studentProfileId);

        if (student.isEmpty()) {
            return new ArrayList<>();
        }

        if (!student.get().getUser().getUsername()
                .equals(authentication.getName())) {
            return new ArrayList<>();
        }

        return reportRepository.findByStudentProfile(student.get());
    }

    public List<Report> getAllReports() {

        return reportRepository.findAll()
                .stream()
                .filter(report ->

                        !report.getStudentProfile()
                                .getUser()
                                .getUsername()
                                .startsWith("test_student")

                                &&

                                !report.getTeacherProfile()
                                        .getUser()
                                        .getUsername()
                                        .startsWith("test_educator")

                )
                .toList();
    }

    @Transactional
    public boolean deleteReport(Long reportId) {

        Optional<Report> report =
                reportRepository.findById(reportId);

        if (report.isEmpty()) {
            return false;
        }

        reportRepository.delete(report.get());

        return true;
    }
}