package com.saptarshi.doubtconnect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.saptarshi.doubtconnect.dto.FavouriteTeacherDTO;
import com.saptarshi.doubtconnect.dto.StudentDto;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.repository.StudentProfileRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
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


    public List<StudentDto> getAll() {

        return studentProfileRepository.findAll()
                .stream()
                .map(student -> {

                    StudentDto dto = new StudentDto();

                    dto.setId(student.getId());
                    dto.setName(student.getUser().getUsername());
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

        if (studentProfile.isEmpty()
                || !isOwner(studentProfile.get(), authentication.getName())) {
            return Optional.empty();
        }

        StudentDto dto = new StudentDto();

        dto.setId(studentProfile.get().getId());
        dto.setName(studentProfile.get().getUser().getUsername());
        dto.setProfilePictureUrl(studentProfile.get().getProfilePictureUrl());
        dto.setGrade(studentProfile.get().getGrade());
        dto.setBoard(studentProfile.get().getBoard());
        dto.setLanguage(studentProfile.get().getLanguage());

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



    public List<TeacherProfile> getFavourites(
            Long id,
            String username){

        Optional<StudentProfile> student =
                studentProfileRepository.findById(id);

        if(student.isEmpty() ||
                !isOwner(student.get(), username)){
            return new ArrayList<>();
        }

        return student.get().getFavourites();
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

        if(!student.get().getFavourites()
                .contains(teacher.get())){

            student.get().getFavourites()
                    .add(teacher.get());

            studentProfileRepository
                    .save(student.get());
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

        student.get().getFavourites()
                .remove(teacher.get());

        studentProfileRepository
                .save(student.get());

        return true;
    }

    @Transactional
    public StudentProfile uploadProfilePicture(
            Long studentProfileId,
            MultipartFile file,
            Authentication authentication) throws IOException {

        Optional<StudentProfile> student =
                studentProfileRepository.findById(studentProfileId);

        if (student.isEmpty()) {
            return null;
        }

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

        return studentProfileRepository.save(student.get());
    }
}