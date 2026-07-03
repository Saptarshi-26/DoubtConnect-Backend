package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.AvailabilityDto;
import com.saptarshi.doubtconnect.dto.AvailabilityResponseDto;
import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.entity.TeacherAvailability;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.google.GoogleCredential;
import com.saptarshi.doubtconnect.google.GoogleCredentialRepository;
import com.saptarshi.doubtconnect.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.annotation.JsonAppend;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TeacherAvailabilityService {

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Autowired
    private SessionRequestRepository sessionRequestRepository;

    @Autowired
    private GoogleCredentialRepository googleCredentialRepository;

    private boolean isOwner(TeacherProfile teacher, String username) {

        Optional<User> user = userRepository.findByUsername(username);

        return teacher.getUser().getUsername().equals(username)
                || (user.isPresent() && "ADMIN".equals(user.get().getRole()));
    }



    @Transactional
    public List<TeacherAvailability> generateMonthlyAvailability(
            Long teacherProfileId,
            Authentication authentication) {

        Optional<TeacherProfile> teacher =
            teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return new ArrayList<>();
        }

        if (!isOwner(teacher.get(), authentication.getName())) {
            return new ArrayList<>();
        }

        Optional<GoogleCredential> credential =
                googleCredentialRepository.findByTeacherProfile(teacher.get());

        if (credential.isEmpty()) {
            throw new RuntimeException("GOOGLE_NOT_CONNECTED");
        }


        LocalDate today = LocalDate.now();

        List<TeacherAvailability> futureSlots =
                teacherAvailabilityRepository
                        .findByTeacherProfileAndEndTimeAfter(
                                teacher.get(),
                                LocalDateTime.now());

        LocalDate startDate;

        if (futureSlots.isEmpty()) {

            startDate = today;

        } else {

            TeacherAvailability lastSlot = futureSlots.stream()
                    .max((a, b) -> a.getEndTime().compareTo(b.getEndTime()))
                    .get();

            LocalDate lastDate = lastSlot.getEndTime().toLocalDate();

            long daysRemaining =
                    java.time.temporal.ChronoUnit.DAYS
                            .between(today, lastDate);

            if (daysRemaining > 10) {
                throw new RuntimeException(
                        "New slots can only be generated when 10 or fewer days remain in your current schedule."
                );
            }

            startDate = lastDate.plusDays(1);
        }

        List<TeacherAvailability> newSlots = new ArrayList<>();

        for (int day = 0; day < 30; day++) {

            LocalDate currentDate = startDate.plusDays(day);

            for (LocalTime time = LocalTime.of(9, 0);
                 time.isBefore(LocalTime.of(18, 0));
                 time = time.plusMinutes(30)) {

                TeacherAvailability slot = new TeacherAvailability();

                slot.setTeacherProfile(teacher.get());

                slot.setStartTime(
                        LocalDateTime.of(currentDate, time));

                slot.setEndTime(
                        LocalDateTime.of(
                                currentDate,
                                time.plusMinutes(30)));

                newSlots.add(slot);
            }
        }

        return teacherAvailabilityRepository.saveAll(newSlots);
    }


    @Transactional
    public List<AvailabilityResponseDto> makeSlotsAvailable(
            Long teacherProfileId,
            List<Long> slotIds,
            Authentication authentication) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return new ArrayList<>();
        }

        if (!isOwner(teacher.get(), authentication.getName())) {
            return new ArrayList<>();
        }

        List<TeacherAvailability> allSlots =
                teacherAvailabilityRepository.findByTeacherProfileOrderByStartTimeAsc(teacher.get());

        // Reset every non-booked slot
        for (TeacherAvailability slot : allSlots) {

            if (!slot.isBooked()) {
                slot.setAvailable(false);
            }
        }

        // Enable selected slots
        List<TeacherAvailability> selectedSlots =
                teacherAvailabilityRepository.findAllById(slotIds);

        for (TeacherAvailability slot : selectedSlots) {

            if (slot.getTeacherProfile().getId().equals(teacherProfileId)
                    && !slot.isBooked()) {

                slot.setAvailable(true);
            }
        }

        return teacherAvailabilityRepository.saveAll(allSlots)
                .stream()
                .map(slot -> {

                    AvailabilityResponseDto dto =
                            new AvailabilityResponseDto();

                    dto.setId(slot.getId());
                    dto.setStartTime(slot.getStartTime());
                    dto.setEndTime(slot.getEndTime());
                    dto.setAvailable(slot.isAvailable());
                    dto.setBooked(slot.isBooked());

                    return dto;

                }).toList();
    }


    public List<AvailabilityResponseDto> getAvailableSlots(
            Long teacherProfileId) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return new ArrayList<>();
        }

        return teacherAvailabilityRepository
                .findByTeacherProfileAndAvailableTrueAndBookedFalse(
                        teacher.get())
                .stream()
                .map(slot -> {

                    AvailabilityResponseDto dto =
                            new AvailabilityResponseDto();

                    dto.setId(slot.getId());
                    dto.setStartTime(slot.getStartTime());
                    dto.setEndTime(slot.getEndTime());
                    dto.setAvailable(slot.isAvailable());
                    dto.setBooked(slot.isBooked());

                    return dto;

                }).toList();
    }


    @Transactional
    public boolean cancelSlots(
            Long teacherProfileId,
            List<Long> slotIds,
            Authentication authentication) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return false;
        }

        if (!isOwner(teacher.get(), authentication.getName())) {
            return false;
        }

        List<TeacherAvailability> slots =
                teacherAvailabilityRepository.findAllById(slotIds);

        for (TeacherAvailability slot : slots) {

            if (!slot.getTeacherProfile().getId().equals(teacherProfileId)) {
                continue;
            }

            slot.setAvailable(false);

            if (slot.isBooked()) {

                Optional<SessionEvent> session =
                        sessionEventRepository
                                .findByTeacherProfileAndStartTimeAndEndTime(
                                        teacher.get(),
                                        slot.getStartTime(),
                                        slot.getEndTime());

                if (session.isPresent()) {
                    session.get().setEventStatus("CANCELLED");
                    session.get().getSessionRequest().setStatus("CANCELLED");
                    sessionRequestRepository.save(session.get().getSessionRequest());
                    sessionEventRepository.save(session.get());
                }
            }
        }

        teacherAvailabilityRepository.saveAll(slots);

        return true;
    }

    public List<AvailabilityResponseDto> getTeacherAvailability(
            Long teacherProfileId,
            Authentication authentication) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return new ArrayList<>();
        }

        if (!isOwner(teacher.get(), authentication.getName())) {
            return new ArrayList<>();
        }

        return teacherAvailabilityRepository
                .findByTeacherProfileOrderByStartTimeAsc(teacher.get())
                .stream()
                .map(slot -> {

                    AvailabilityResponseDto dto =
                            new AvailabilityResponseDto();

                    dto.setId(slot.getId());
                    dto.setStartTime(slot.getStartTime());
                    dto.setEndTime(slot.getEndTime());
                    dto.setAvailable(slot.isAvailable());
                    dto.setBooked(slot.isBooked());

                    return dto;

                }).toList();
    }
   }

