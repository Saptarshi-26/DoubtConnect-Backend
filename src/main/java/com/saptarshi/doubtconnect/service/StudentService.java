package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.FavouriteTeacherDTO;
import com.saptarshi.doubtconnect.entity.StudentProfile;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.repository.StudentProfileRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    StudentProfileRepository studentProfileRepository;

    @Autowired
    TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<StudentProfile> findStudent(Long id){
        return studentProfileRepository.findById(id);
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
}