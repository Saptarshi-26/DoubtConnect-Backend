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

        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);

        if (teacher.isEmpty()) {
            return new ArrayList<>();
        }

        if (!isOwner(teacher.get(), authentication.getName())) {
            return new ArrayList<>();
        }

        Optional<TeacherMeetingDetails> meetingDetails =
                teacherMeetingDetailsRepository
                        .findByTeacherProfile(teacher.get());

        if (meetingDetails.isEmpty()
                || meetingDetails.get().getMeetingLink() == null
                || meetingDetails.get().getMeetingLink().isBlank()) {

            throw new RuntimeException("MEETING_LINK_NOT_SET");
        }

        LocalDate today = LocalDate.now();

        List<TeacherAvailability> futureSlots =
                teacherAvailabilityRepository
                        .findByTeacherProfileAndEndTimeAfter(
                                teacher.get(),
                                LocalDateTime.now());

        LocalDate startDate;

        if (futureSlots.isEmpty()) {

            if (LocalTime.now().isBefore(LocalTime.of(18, 0))) {
                startDate = today;
            } else {
                startDate = today.plusDays(1);
            }

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

                LocalDateTime slotStart =
                        LocalDateTime.of(currentDate, time);

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

        return teacherAvailabilityRepository.saveAll(newSlots);
    }

    @Transactional
    public List<AvailabilityResponseDto> makeSlotsAvailable(
            Long teacherProfileId,
            List<Long> slotIds,
            Authentication authentication) {
       System.out.println("ENTERED makeSlotsAvailable");
        Optional<TeacherProfile> teacher =
                teacherProfileRepository.findById(teacherProfileId);
      //  System.out.println("1");
        if (teacher.isEmpty()) {
            return new ArrayList<>();
        }
       System.out.println("2");
        if (!isOwner(teacher.get(), authentication.getName())) {
            return new ArrayList<>();
        }
        System.out.println("3");
        List<TeacherAvailability> allSlots =
                teacherAvailabilityRepository.findByTeacherProfileOrderByStartTimeAsc(teacher.get());

        // Reset every non-booked slot
       System.out.println("4");
        for (TeacherAvailability slot : allSlots) {

            if (!slot.isBooked()) {
                slot.setAvailable(false);
            }
        }System.out.println("5");
        // Enable selected slots
        // Enable selected slots
        List<TeacherAvailability> selectedSlots =
                teacherAvailabilityRepository.findAllById(slotIds);
       System.out.println("6");
        for (TeacherAvailability slot : selectedSlots) {

            if (!slot.getTeacherProfile().getId().equals(teacherProfileId)) {
                continue;
            }

            if (slot.isBooked()) {
                continue;
            }

            System.out.println("Current = " + LocalDateTime.now());
            System.out.println("Slot    = " + slot.getStartTime());
            System.out.println(slot.getStartTime());
            System.out.println(LocalDateTime.now());

            if (!slot.getStartTime().isAfter(LocalDateTime.now())) {
                throw new RuntimeException(
                        "Past or current time slots cannot be made available.");
            }
            slot.setAvailable(true);
        }
        System.out.println("7");
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

