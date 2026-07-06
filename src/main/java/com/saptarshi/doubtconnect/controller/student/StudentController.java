package com.saptarshi.doubtconnect.controller.student;

import com.saptarshi.doubtconnect.dto.FavouriteTeacherDTO;
import com.saptarshi.doubtconnect.dto.StudentDto;
import com.saptarshi.doubtconnect.dto.UpdateStudentDto;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/student")

public class StudentController {

    @Autowired
    private StudentService studentService;

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(
            @PathVariable Long id,
            Authentication authentication) {

        boolean deleted =
                studentService.deleteStudent(
                        id,
                        authentication);

        return deleted
                ? new ResponseEntity<>(
                "Student deleted successfully",
                HttpStatus.OK)
                : new ResponseEntity<>(
                "Student not found or unauthorized",
                HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll(){
        return new ResponseEntity<>(studentService.getAll(),HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findStudent(
            @PathVariable Long id, Authentication authentication){

        Optional<StudentDto> student =
                studentService.findStudent(id,authentication);

        return student.isPresent()
                ? new ResponseEntity<>(student.get(),
                HttpStatus.OK)
                : new ResponseEntity<>("Student not found",
                HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}/favourites")
    public ResponseEntity<?> getFavourites(
            @PathVariable Long id,
            Authentication authentication){

        List<TeacherProfile> favourites =
                studentService.getFavourites(
                        id,
                        authentication.getName());

        return new ResponseEntity<>(
                favourites,
                HttpStatus.OK);
    }

    @PostMapping("/favourite")
    public ResponseEntity<?> addFavouriteTeacher(
            @RequestBody FavouriteTeacherDTO dto,
            Authentication authentication){

        boolean added =
                studentService.addFavouriteTeacher(
                        dto,
                        authentication.getName());

        return added
                ? new ResponseEntity<>("Teacher Added",
                HttpStatus.OK)
                : new ResponseEntity<>("Unauthorized",
                HttpStatus.FORBIDDEN);
    }

    @DeleteMapping("/favourite")
    public ResponseEntity<?> removeFavouriteTeacher(
            @RequestBody FavouriteTeacherDTO dto,
            Authentication authentication){

        boolean removed =
                studentService.removeFavouriteTeacher(
                        dto,
                        authentication.getName());

        return removed
                ? new ResponseEntity<>("Teacher Removed",
                HttpStatus.OK)
                : new ResponseEntity<>("Unauthorized",
                HttpStatus.FORBIDDEN);
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<StudentProfile> uploadProfilePicture(
            @RequestParam Long studentProfileId,
            @RequestParam MultipartFile file,
            Authentication authentication) throws IOException {

        StudentProfile student =
                studentService.uploadProfilePicture(
                        studentProfileId,
                        file,
                        authentication);

        if (student == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProfile(
            @PathVariable Long id,
            @RequestBody UpdateStudentDto dto,
            Authentication authentication) {

        boolean updated = studentService.updateProfile(id, dto, authentication.getName());

        return updated
                ? new ResponseEntity<>("Profile updated", HttpStatus.OK)
                : new ResponseEntity<>("Unable to update profile", HttpStatus.BAD_REQUEST);
    }
}