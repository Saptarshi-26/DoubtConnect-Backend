package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.StudentDto;
import com.saptarshi.doubtconnect.dto.TeacherDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test-data")
public class TestDataController {

    private final com.saptarshi.doubtconnect.testdata.TestDataService testDataService;

    public TestDataController(com.saptarshi.doubtconnect.testdata.TestDataService testDataService) {
        this.testDataService = testDataService;
    }

    @GetMapping("/teachers")
    public List<TeacherDto> getTestTeachers() {
        return testDataService.getTestTeachers();
    }

    @GetMapping("/students")
    public List<StudentDto> getTestStudents() {
        return testDataService.getTestStudents();
    }
}