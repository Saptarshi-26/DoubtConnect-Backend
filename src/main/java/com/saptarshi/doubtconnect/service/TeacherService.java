package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.SubjectDTO;
import com.saptarshi.doubtconnect.dto.UpdateBioDTO;
import com.saptarshi.doubtconnect.dto.UpdateRateDTO;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
import org.hibernate.annotations.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private UserRepository userRepository;


    public List<TeacherProfile> findAll(){
        return teacherProfileRepository.findAll();
    }

    public Optional<TeacherProfile> findTeacher(Long id){
        return teacherProfileRepository.findById(id);
    }

    private boolean isOwner(
            TeacherProfile teacher,
            String username){

        Optional<User> user = userRepository.findByUsername(username);

        return teacher.getUser()
                .getUsername()
                .equals(username) ||
                 user.isPresent() && user.get().getRole().equals("ADMIN");
    }

    public boolean updateBio(
            Long id,
            UpdateBioDTO dto,
            String username){

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(id);

        if(teacher.isEmpty() ||
                !isOwner(teacher.get(), username)){
            return false;
        }

        teacher.get().setBio(dto.getBio());
        teacherProfileRepository.save(teacher.get());

        return true;
    }

    public boolean updateRate(
            Long id,
            UpdateRateDTO dto,
            String username){

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(id);

        if(teacher.isEmpty() ||
                !isOwner(teacher.get(), username)){
            return false;
        }

        teacher.get().setRatePerThirtyMin(
                dto.getRatePerThirtyMin());

        teacherProfileRepository.save(teacher.get());

        return true;
    }

    public boolean addSubject(
            Long id,
            SubjectDTO dto,
            String username){

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(id);

        if(teacher.isEmpty() ||
                !isOwner(teacher.get(), username)){
            return false;
        }

        if(!teacher.get().getSubjects()
                .contains(dto.getSubject())){

            teacher.get().getSubjects()
                    .add(dto.getSubject());

            teacherProfileRepository.save(
                    teacher.get());
        }

        return true;
    }

    public boolean removeSubject(
            Long id,
            SubjectDTO dto,
            String username){

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(id);

        if(teacher.isEmpty() ||
                !isOwner(teacher.get(), username)){
            return false;
        }

        teacher.get().getSubjects()
                .remove(dto.getSubject());

        teacherProfileRepository.save(
                teacher.get());

        return true;
    }
}