package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.dto.AvailabilityDto;
import com.saptarshi.doubtconnect.dto.AvailabilityResponseDto;
import com.saptarshi.doubtconnect.entity.*;
import com.saptarshi.doubtconnect.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.annotation.JsonAppend;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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
    private TeacherMeetingDetailsRepository teacherMeetingDetailsRepository;



    @Autowired
    private  StudentProfileRepository studentProfileRepository;

    private boolean isOwner(TeacherProfile teacher, String username) {

        Optional<User> user = userRepository.findByUsername(username);

        return teacher.getUser().getUsername().equals(username)
                || (user.isPresent() && "ADMIN".equals(user.get().getRole()));
    }



    @Transactional
    public List<TeacherAvailability> generateMonthlyAvailability(
            Long teacherProfileId,
            Authentication authentication) {

        long total = System.currentTimeMillis();
        System.out.println("=== generateMonthlyAvailability START (teacherProfileId=" + teacherProfileId + ") ===");

        long t = System.currentTimeMillis();
        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);
        System.out.println("[TIMER] Teacher lookup = " + (System.currentTimeMillis() - t) + " ms");

        if (teacher.isEmpty()) {
            System.out.println("Teacher not found, aborting.");
            return new ArrayList<>();
        }

        if (!isOwner(teacher.get(), authentication.getName())) {
            System.out.println("Auth check failed: caller is not owner, aborting.");
            return new ArrayList<>();
        }

        t = System.currentTimeMillis();
        Optional<TeacherMeetingDetails> meetingDetails =
                teacherMeetingDetailsRepository.findByTeacherProfile(teacher.get());
        System.out.println("[TIMER] Meeting details lookup = " + (System.currentTimeMillis() - t) + " ms");

        if (meetingDetails.isEmpty()
                || meetingDetails.get().getMeetingLink() == null
                || meetingDetails.get().getMeetingLink().isBlank()) {
            System.out.println("Meeting link not set, aborting.");
            throw new RuntimeException("MEETING_LINK_NOT_SET");
        }

        LocalDate today = LocalDate.now();

        t = System.currentTimeMillis();
        List<TeacherAvailability> futureSlots =
                teacherAvailabilityRepository.findByTeacherProfileAndEndTimeAfter(
                        teacher.get(), LocalDateTime.now());
        System.out.println("[TIMER] Future slot lookup = " + (System.currentTimeMillis() - t) + " ms");
        System.out.println("Existing future slot count = " + futureSlots.size());

        LocalDate startDate;

        if (futureSlots.isEmpty()) {
            startDate = LocalTime.now().isBefore(LocalTime.of(18, 0)) ? today : today.plusDays(1);
            System.out.println("No existing slots. startDate = " + startDate);
        } else {
            TeacherAvailability lastSlot = futureSlots.stream()
                    .max((a, b) -> a.getEndTime().compareTo(b.getEndTime()))
                    .get();

            LocalDate lastDate = lastSlot.getEndTime().toLocalDate();

            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, lastDate);
            System.out.println("Last scheduled date = " + lastDate + ", daysRemaining = " + daysRemaining);

            if (daysRemaining > 10) {
                System.out.println("Blocked: more than 10 days remain in existing schedule.");
                throw new RuntimeException(
                        "New slots can only be generated when 10 or fewer days remain in your current schedule.");
            }

            startDate = lastDate.plusDays(1);
            System.out.println("startDate = " + startDate);
        }

        t = System.currentTimeMillis();
        List<TeacherAvailability> newSlots = new ArrayList<>();

        for (int day = 0; day < 30; day++) {
            LocalDate currentDate = startDate.plusDays(day);

            for (LocalTime time = LocalTime.of(9, 0);
                 time.isBefore(LocalTime.of(18, 0));
                 time = time.plusMinutes(30)) {

                LocalDateTime slotStart = LocalDateTime.of(currentDate, time);

                if (slotStart.isBefore(LocalDateTime.now())) {
                    continue;
                }

                TeacherAvailability slot = new TeacherAvailability();
                slot.setTeacherProfile(teacher.get());
                slot.setStartTime(slotStart);
                slot.setEndTime(slotStart.plusMinutes(30));

                newSlots.add(slot);
            }
        }
        System.out.println("[TIMER] Slot generation loop = " + (System.currentTimeMillis() - t) + " ms");
        System.out.println("Slots generated (pre-save) = " + newSlots.size());

        t = System.currentTimeMillis();
        List<TeacherAvailability> saved = teacherAvailabilityRepository.saveAll(newSlots);
        teacherAvailabilityRepository.flush();

        System.out.println("[TIMER] saveAll = " + (System.currentTimeMillis() - t) + " ms");
        System.out.println("Slots saved = " + saved.size());

        System.out.println("[TIMER] TOTAL = " + (System.currentTimeMillis() - total) + " ms");
        System.out.println("=== generateMonthlyAvailability COMPLETE ===");

        return saved;
    }

    @Transactional
    public List<AvailabilityResponseDto> makeSlotsAvailable(
            Long teacherProfileId,
            List<Long> slotIds,
            Authentication authentication) {

        long total = System.currentTimeMillis();
        System.out.println("=== makeSlotsAvailable START (teacherProfileId=" + teacherProfileId + ", requestedSlotIds=" + slotIds.size() + ") ===");

        long t = System.currentTimeMillis();
        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);
        System.out.println("[TIMER] Teacher lookup = " + (System.currentTimeMillis() - t) + " ms");

        if (teacher.isEmpty()) {
            System.out.println("Teacher not found, aborting.");
            return new ArrayList<>();
        }
        if (!isOwner(teacher.get(), authentication.getName())) {
            System.out.println("Auth check failed: caller is not owner, aborting.");
            return new ArrayList<>();
        }

        t = System.currentTimeMillis();

        int resetCount =
                teacherAvailabilityRepository.resetAvailability(teacher.get());

        System.out.println("Bulk reset = " + (System.currentTimeMillis() - t) + " ms");
        System.out.println("Slots reset = " + resetCount);
        System.out.println("[TIMER] Reset non-booked slots loop = " + (System.currentTimeMillis() - t) + " ms");
        System.out.println("Slots reset to unavailable = " + resetCount);

        t = System.currentTimeMillis();
        List<TeacherAvailability> selectedSlots =
                teacherAvailabilityRepository.findAllById(slotIds);
        System.out.println("[TIMER] Selected slots lookup = " + (System.currentTimeMillis() - t) + " ms");
        System.out.println("Selected slots found = " + selectedSlots.size());

        t = System.currentTimeMillis();
        int enabledCount = 0;
        for (TeacherAvailability slot : selectedSlots) {

            if (!slot.getTeacherProfile().getId().equals(teacherProfileId)) {
                continue;
            }

            if (slot.isBooked()) {
                continue;
            }
            System.out.println("Current server time = " + LocalDateTime.now());
            System.out.println("Slot start time     = " + slot.getStartTime());
            if (!slot.getStartTime().isAfter(LocalDateTime.now())) {
                System.out.println("Rejected: slot " + slot.getId() + " is in the past.");
                throw new RuntimeException("Past or current time slots cannot be made available.");
            }

            slot.setAvailable(true);
            enabledCount++;
        }
        System.out.println("[TIMER] Enable selected slots loop = " + (System.currentTimeMillis() - t) + " ms");
        System.out.println("Slots enabled = " + enabledCount);

        t = System.currentTimeMillis();
        List<TeacherAvailability> saved =
                teacherAvailabilityRepository.saveAll(selectedSlots);        System.out.println("[TIMER] saveAll = " + (System.currentTimeMillis() - t) + " ms");
        System.out.println("Slots saved = " + saved.size());

        t = System.currentTimeMillis();
        List<AvailabilityResponseDto> result = saved.stream()
                .map(slot -> {
                    AvailabilityResponseDto dto = new AvailabilityResponseDto();
                    dto.setId(slot.getId());
                    dto.setStartTime(slot.getStartTime());
                    dto.setEndTime(slot.getEndTime());
                    dto.setAvailable(slot.isAvailable());
                    dto.setBooked(slot.isBooked());
                    return dto;
                }).toList();
        System.out.println("[TIMER] DTO mapping = " + (System.currentTimeMillis() - t) + " ms");

        System.out.println("[TIMER] TOTAL = " + (System.currentTimeMillis() - total) + " ms");
        System.out.println("=== makeSlotsAvailable COMPLETE ===");

        return result;
    }


    public List<AvailabilityResponseDto> getAvailableSlots(
            Long teacherProfileId , long studentProfileId,Authentication authentication) {

        Optional<StudentProfile> studentProfile = studentProfileRepository.findById(studentProfileId);
        if(studentProfile.isEmpty()) return new ArrayList<>();
        if(!studentProfile.get().getUser().getUsername().equals(authentication.getName()))
            return  new ArrayList<>();

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return new ArrayList<>();
        }

        return teacherAvailabilityRepository
                .findByTeacherProfileAndAvailableTrueAndBookedFalseAndStartTimeAfter(
                        teacher.get(),
                        LocalDateTime.now()
                )
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

        if (teacher.isEmpty()) return false;
        if (!isOwner(teacher.get(), authentication.getName())) return false;

        List<TeacherAvailability> requestedSlots =
                teacherAvailabilityRepository.findAllById(slotIds);

        List<SessionEvent> teacherEvents =
                sessionEventRepository.findByTeacherProfile(teacher.get());

        Map<Long, SessionEvent> eventsToCancel = new HashMap<>();

        for (TeacherAvailability slot : requestedSlots) {
            System.out.println("Current server time = " + LocalDateTime.now());
            System.out.println("Slot start time     = " + slot.getStartTime());
            if (!slot.getTeacherProfile().getId().equals(teacherProfileId)) {
                continue;
            }

            if (slot.isBooked()) {

                teacherEvents.stream()
                        .filter(e -> !"CANCELLED".equals(e.getEventStatus())
                                && !"COMPLETED".equals(e.getEventStatus()))
                        .filter(e -> !slot.getStartTime().isBefore(e.getStartTime())
                                && !slot.getEndTime().isAfter(e.getEndTime()))
                        .findFirst()
                        .ifPresent(event -> eventsToCancel.put(event.getId(), event));

            } else {
                slot.setAvailable(false);
            }
        }

        for (SessionEvent event : eventsToCancel.values()) {

            // Cannot cancel ongoing or completed sessions
            if (!event.getStartTime().isAfter(LocalDateTime.now())) {
                throw new RuntimeException(
                        "Ongoing or completed sessions cannot be cancelled.");
            }

            event.setEventStatus("CANCELLED");
            event.getSessionRequest().setStatus("CANCELLED");

            sessionRequestRepository.save(event.getSessionRequest());
            sessionEventRepository.save(event);

            List<TeacherAvailability> bookedSlots =
                    teacherAvailabilityRepository
                            .findByTeacherProfileAndBookedTrueOrderByStartTimeAsc(
                                    teacher.get());

            for (TeacherAvailability s : bookedSlots) {
                if (!s.getStartTime().isBefore(event.getStartTime())
                        && !s.getEndTime().isAfter(event.getEndTime())) {
                    s.setBooked(false);
                    s.setAvailable(false);
                }
            }

            teacherAvailabilityRepository.saveAll(bookedSlots);
        }

        teacherAvailabilityRepository.saveAll(requestedSlots);

        return !eventsToCancel.isEmpty();
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

