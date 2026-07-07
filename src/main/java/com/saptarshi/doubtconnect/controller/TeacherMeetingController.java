package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.TeacherMeetingDetailsDto;
import com.saptarshi.doubtconnect.entity.TeacherMeetingDetails;
import com.saptarshi.doubtconnect.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teacher-meeting")
public class TeacherMeetingController {

    @Autowired
    private TeacherService teacherService;

    @PostMapping("/{teacherProfileId}")
    public TeacherMeetingDetails save(
            @PathVariable Long teacherProfileId,
            @RequestBody TeacherMeetingDetailsDto dto,
            Authentication authentication){

        return teacherService.saveMeetingDetails(
                teacherProfileId,
                dto,
                authentication);
    }

    @GetMapping("/{teacherProfileId}")
    public TeacherMeetingDetailsDto get(
            @PathVariable Long teacherProfileId,
            Authentication authentication){

        return teacherService.getMeetingDetails(
                teacherProfileId,
                authentication);
    }
}