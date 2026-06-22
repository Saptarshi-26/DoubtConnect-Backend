package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.RatingDto;
import com.saptarshi.doubtconnect.dto.SubjectDTO;
import com.saptarshi.doubtconnect.dto.UpdateBioDTO;
import com.saptarshi.doubtconnect.dto.UpdateRateDTO;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/getAll")
    public ResponseEntity<?> findAll(){
        List<TeacherProfile> profileList = teacherService.findAll();
        return profileList.isEmpty()?new ResponseEntity<>("No teacher found",HttpStatus.OK):
                new ResponseEntity<>(profileList,HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> findTeacher(
            @PathVariable Long id){

        Optional<TeacherProfile> teacher =
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
}