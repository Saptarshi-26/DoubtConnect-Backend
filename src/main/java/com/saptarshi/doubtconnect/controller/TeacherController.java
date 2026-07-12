package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.*;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeacher(
            @PathVariable Long id,
            Authentication authentication) {

        boolean deleted =
                teacherService.deleteTeacher(
                        id,
                        authentication);

        return deleted
                ? new ResponseEntity<>(
                "Teacher deleted successfully",
                HttpStatus.OK)
                : new ResponseEntity<>(
                "Teacher not found or unauthorized",
                HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> findAll(Authentication authentication) {

        long start = System.currentTimeMillis();

        List<TeacherDto> profileList = teacherService.findAll(authentication);

        ResponseEntity<?> response = profileList.isEmpty()
                ? new ResponseEntity<>("No teacher found", HttpStatus.OK)
                : new ResponseEntity<>(profileList, HttpStatus.OK);

        System.out.println("GET /teacher/getAll total time = "
                + (System.currentTimeMillis() - start) + " ms");

        return response;
    }

    @GetMapping("/getAllInternal")
    public ResponseEntity<?> findAllInternal(){
        List<TeacherDto> profileList = teacherService.findAllInternal();
        return profileList.isEmpty()?new ResponseEntity<>("No teacher found",HttpStatus.OK):
                new ResponseEntity<>(profileList,HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<TeacherDto>> searchBySubject(
            @RequestParam String subject,
            Authentication authentication) {

        return ResponseEntity.ok(
                teacherService.searchBySubject(
                        subject,
                        authentication));
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> findTeacher(
            @PathVariable Long id){

        Optional<TeacherDto> teacher =
                teacherService.findTeacher(id);

        return teacher.isPresent()
                ? new ResponseEntity<>(teacher.get(), HttpStatus.OK)
                : new ResponseEntity<>("Teacher not found",
                HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{id}/bio")
    public ResponseEntity<?> updateBio(
            @PathVariable Long id,
            @RequestBody UpdateBioDTO dto,
            Authentication authentication){

        boolean updated =
                teacherService.updateBio(
                        id,
                        dto,
                        authentication.getName());

        return updated
                ? new ResponseEntity<>("Bio Updated",
                HttpStatus.OK)
                : new ResponseEntity<>("Unauthorized",
                HttpStatus.FORBIDDEN);
    }

    @PutMapping("/{id}/rate")
    public ResponseEntity<?> updateRate(
            @PathVariable Long id,
            @RequestBody UpdateRateDTO dto,
            Authentication authentication){

        boolean updated =
                teacherService.updateRate(
                        id,
                        dto,
                        authentication.getName());

        return updated
                ? new ResponseEntity<>("Rate Updated",
                HttpStatus.OK)
                : new ResponseEntity<>("Unauthorized",
                HttpStatus.FORBIDDEN);
    }

    @PostMapping("/{id}/subject")
    public ResponseEntity<?> addSubject(
            @PathVariable Long id,
            @RequestBody SubjectDTO dto,
            Authentication authentication){

        boolean added =
                teacherService.addSubject(
                        id,
                        dto,
                        authentication.getName());

        return added
                ? new ResponseEntity<>("Subject Added",
                HttpStatus.OK)
                : new ResponseEntity<>("Unauthorized",
                HttpStatus.FORBIDDEN);
    }

    @DeleteMapping("/{id}/subject")
    public ResponseEntity<?> removeSubject(
            @PathVariable Long id,
            @RequestBody SubjectDTO dto,
            Authentication authentication){

        boolean removed =
                teacherService.removeSubject(
                        id,
                        dto,
                        authentication.getName());

        return removed
                ? new ResponseEntity<>("Subject Removed",
                HttpStatus.OK)
                : new ResponseEntity<>("Unauthorized",
                HttpStatus.FORBIDDEN);
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<TeacherDto> uploadProfilePicture(
            @RequestParam Long teacherProfileId,
            @RequestParam MultipartFile file,
            Authentication authentication) throws IOException {

        TeacherDto teacher =
                teacherService.uploadProfilePicture(
                        teacherProfileId,
                        file,
                        authentication);

        if (teacher == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(teacher);
    }
}