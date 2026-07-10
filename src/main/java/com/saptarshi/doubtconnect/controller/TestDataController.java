package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.StudentDto;
import com.saptarshi.doubtconnect.dto.TeacherDto;
import com.saptarshi.doubtconnect.service.TestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test-data")
public class TestDataController {

   @Autowired
   private TestDataService testDataService;

    @GetMapping("/teachers")
    public List<TeacherDto> getTestTeachers(Authentication authentication) {
        return testDataService.getAllTestTeachers(authentication);
    }

    @GetMapping("/students")
    public List<StudentDto> getTestStudents() {
        return testDataService.getTestStudents();
    }
    @GetMapping("/getAll")
    public List<TeacherDto> getAllTestTeachers(
            Authentication authentication) {

        return testDataService.getAllTestTeachers(authentication);
    }
    @GetMapping("/search")
    public List<TeacherDto> searchTestTeachers(
            @RequestParam String subject,
            Authentication authentication) {

        return testDataService.searchTestTeachers(
                subject,
                authentication);
    }
    @GetMapping("/favourites/{studentId}")
    public List<TeacherDto> getFavouriteTestTeachers(
            @PathVariable Long studentId) {

        return testDataService.getFavouriteTestTeachers(studentId);
    }
}