package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.AvailabilityDto;
import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.entity.TeacherAvailability;
import com.saptarshi.doubtconnect.entity.TeacherProfile;
import com.saptarshi.doubtconnect.entity.User;
import com.saptarshi.doubtconnect.repository.SessionEventRepository;
import com.saptarshi.doubtconnect.repository.TeacherAvailabilityRepository;
import com.saptarshi.doubtconnect.repository.TeacherProfileRepository;
import com.saptarshi.doubtconnect.repository.UserRepository;
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

    private boolean isOwner(TeacherProfile teacher, String username) {

        Optional<User> user = userRepository.findByUsername(username);

        return teacher.getUser().getUsername().equals(username)
                || (user.isPresent() && "ADMIN".equals(user.get().getRole()));
    }
    private List<TeacherAvailability> generateMonthlyAvailability(
            TeacherProfile teacher) {

        List<TeacherAvailability> existing =
                teacherAvailabilityRepository.findByTeacherProfile(teacher);

        if (!existing.isEmpty()) {
            return existing;
        }

        LocalDate today = LocalDate.now();

        List<TeacherAvailability> slots = new ArrayList<>();

        for (int day = 0; day < 30; day++) {

            LocalDate currentDate = today.plusDays(day);

            for (LocalTime time = LocalTime.of(9, 0);
                 time.isBefore(LocalTime.of(18, 0));
                 time = time.plusMinutes(30)) {

                TeacherAvailability slot = new TeacherAvailability();

                slot.setTeacherProfile(teacher);

                slot.setStartTime(
                        LocalDateTime.of(currentDate, time));

                slot.setEndTime(
                        LocalDateTime.of(
                                currentDate,
                                time.plusMinutes(30)));

                slots.add(slot);
            }
        }

        return teacherAvailabilityRepository.saveAll(slots);
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

        return generateMonthlyAvailability(teacher.get());
    }
    public List<TeacherAvailability> getTeacherAvailability(
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

        List<TeacherAvailability> slots =
                teacherAvailabilityRepository.findByTeacherProfile(teacher.get());

        if (slots.isEmpty()) {
            return generateMonthlyAvailability(teacher.get());
        }

        return slots;
    }
    @Transactional
    public List<TeacherAvailability> makeSlotsAvailable(
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
                teacherAvailabilityRepository.findByTeacherProfile(teacher.get());

        if (allSlots.isEmpty()) {
            allSlots = generateMonthlyAvailability(teacher.get());
        }

        // Reset every non-booked slot
        for (TeacherAvailability slot : allSlots) {

            if (!slot.isBooked()) {
                slot.setAvailable(false);
            }
        }

        // Enable only selected slots
        List<TeacherAvailability> selectedSlots =
                teacherAvailabilityRepository.findAllById(slotIds);

        for (TeacherAvailability slot : selectedSlots) {

            if (slot.getTeacherProfile().getId().equals(teacherProfileId)
                    && !slot.isBooked()) {

                slot.setAvailable(true);
            }
        }

        return teacherAvailabilityRepository.saveAll(allSlots);
    }
    public List<TeacherAvailability> getAvailableSlots(
            Long teacherProfileId) {

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return new ArrayList<>();
        }

        List<TeacherAvailability> slots =
                teacherAvailabilityRepository.findByTeacherProfile(teacher.get());

        if (slots.isEmpty()) {
            generateMonthlyAvailability(teacher.get());
        }

        return teacherAvailabilityRepository
                .findByTeacherProfileAndAvailableTrueAndBookedFalse(
                        teacher.get());
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

        List<SessionEvent> sessions =
                sessionEventRepository.findByTeacherProfileAndEventStatus(
                        teacher.get(),
                        "UPCOMING"
                );

        for (TeacherAvailability slot : slots) {

            if (!slot.getTeacherProfile().getId().equals(teacherProfileId)) {
                continue;
            }

            slot.setAvailable(false);

            if (slot.isBooked()) {

                for (SessionEvent session : sessions) {

                    if (session.getStartTime().equals(slot.getStartTime())
                            && session.getEndTime().equals(slot.getEndTime())) {

                        session.setEventStatus("CANCELLED");
                        break;
                    }
                }

                // Keep booked = true so the cancelled slot
                // cannot be booked again accidentally.
            }
        }

        teacherAvailabilityRepository.saveAll(slots);
        sessionEventRepository.saveAll(sessions);

        return true;
    }

   }

