package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.dto.AvailabilityDto;
import com.saptarshi.doubtconnect.dto.AvailabilityResponseDto;
import com.saptarshi.doubtconnect.entity.TeacherAvailability;
import com.saptarshi.doubtconnect.service.TeacherAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher-availability")
public class TeacherAvailabilityController {

    @Autowired
    private TeacherAvailabilityService teacherAvailabilityService;

    @PostMapping("/generate/{teacherId}")
    public List<TeacherAvailability> generateSlots(
            @PathVariable Long teacherId,
            Authentication authentication) {

        return teacherAvailabilityService
                .generateMonthlyAvailability(
                        teacherId,
                        authentication);
    }

    @GetMapping("/{teacherId}")
    public List<AvailabilityResponseDto> getTeacherAvailability(
            @PathVariable Long teacherId,
            Authentication authentication) {

        return teacherAvailabilityService
                .getTeacherAvailability(
                        teacherId,
                        authentication);
    }

    @PutMapping("/available/{teacherId}")
    public List<AvailabilityResponseDto> makeAvailable(
            @PathVariable Long teacherId,
            @RequestBody List<Long> slotIds,
            Authentication authentication) {

        return teacherAvailabilityService
                .makeSlotsAvailable(
                        teacherId,
                        slotIds,
                        authentication);
    }

    @PutMapping("/cancel/{teacherId}")
    public boolean cancelSlots(
            @PathVariable Long teacherId,
            @RequestBody List<Long> slotIds,
            Authentication authentication) {

        return teacherAvailabilityService
                .cancelSlots(
                        teacherId,
                        slotIds,
                        authentication);
    }

    @GetMapping("/student/{teacherId}/{studentId}")
    public List<AvailabilityResponseDto> getAvailableSlots(
            @PathVariable Long teacherId,@PathVariable long studentId , Authentication authentication) {

        return teacherAvailabilityService
                .getAvailableSlots(teacherId,studentId,authentication);
    }
}