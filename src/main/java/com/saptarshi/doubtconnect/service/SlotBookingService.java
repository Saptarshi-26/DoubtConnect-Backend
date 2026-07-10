package com.saptarshi.doubtconnect.service;

import com.saptarshi.doubtconnect.entity.*;
import com.saptarshi.doubtconnect.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SlotBookingService {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private SessionRequestRepository sessionRequestRepository;

    @Autowired
    private SessionEventRepository sessionEventRepository;

    @Autowired
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

    @Autowired
    private TeacherMeetingDetailsRepository teacherMeetingDetailsRepository;


    private boolean isOwner(String username , Authentication authentication){
        return username.equals(authentication.getName());
    }

    private List<TeacherAvailability> checkFront(
            TeacherAvailability clickedSlot,
            int slotsNeeded) {

        List<TeacherAvailability> availableSlots =
                teacherAvailabilityRepository
                        .findByTeacherProfileAndAvailableTrueAndBookedFalseOrderByStartTimeAsc(
                                clickedSlot.getTeacherProfile());

        int startIndex = -1;

        for (int i = 0; i < availableSlots.size(); i++) {

            if (availableSlots.get(i).getId() == clickedSlot.getId()) {
                startIndex = i;
                break;
            }
        }

        if (startIndex == -1) {
            return null;
        }

        if (startIndex + slotsNeeded > availableSlots.size()) {
            return null;
        }

        List<TeacherAvailability> selectedSlots = new ArrayList<>();

        for (int i = startIndex; i < startIndex + slotsNeeded; i++) {

            TeacherAvailability current = availableSlots.get(i);

            if (!selectedSlots.isEmpty()) {

                TeacherAvailability previous =
                        selectedSlots.get(selectedSlots.size() - 1);

                if (!previous.getEndTime().equals(current.getStartTime())) {
                    return null;
                }
            }

            selectedSlots.add(current);
        }

        return selectedSlots;
    }


    private List<TeacherAvailability> checkBothSides(
            TeacherAvailability clickedSlot,
            int slotsNeeded) {

        List<TeacherAvailability> availableSlots =
                teacherAvailabilityRepository
                        .findByTeacherProfileAndAvailableTrueAndBookedFalseOrderByStartTimeAsc(
                                clickedSlot.getTeacherProfile());

        int clickedIndex = -1;

        for (int i = 0; i < availableSlots.size(); i++) {

            if (availableSlots.get(i).getId() == clickedSlot.getId()) {
                clickedIndex = i;
                break;
            }
        }

        if (clickedIndex == -1) {
            return null;
        }

        // Try different balanced combinations
        for (int before = 1; before < slotsNeeded; before++) {

            int after = slotsNeeded - before - 1;

            int start = clickedIndex - before;
            int end = clickedIndex + after;

            if (start < 0 || end >= availableSlots.size()) {
                continue;
            }

            List<TeacherAvailability> selected = new ArrayList<>();
            boolean valid = true;

            for (int i = start; i <= end; i++) {

                TeacherAvailability current = availableSlots.get(i);

                if (!selected.isEmpty()) {

                    TeacherAvailability previous =
                            selected.get(selected.size() - 1);

                    if (!previous.getEndTime().equals(current.getStartTime())) {
                        valid = false;
                        break;
                    }
                }

                selected.add(current);
            }

            if (valid) {
                return selected;
            }
        }

        return null;
    }



    @Transactional
    public SessionEvent bookSlot(
            Long sessionRequestId,
            Long slotId,
            Authentication authentication) {

        Optional<SessionRequest> sessionRequest =
                sessionRequestRepository.findById(sessionRequestId);

        if (sessionRequest.isEmpty()) {
            return null;
        }

        if (!isOwner(
                sessionRequest.get()
                        .getStudentProfile()
                        .getUser()
                        .getUsername(),
                authentication)) {
            return null;
        }

        if (!"ACCEPTED".equals(sessionRequest.get().getStatus())) {
            return null;
        }

        Optional<TeacherAvailability> slot =
                teacherAvailabilityRepository.findById(slotId);

        if (slot.isEmpty()) {
            return null;
        }

        if (!slot.get().getTeacherProfile().getId().equals(
                sessionRequest.get().getTeacherProfile().getId())) {
            return null;
        }

        if (!slot.get().isAvailable() || slot.get().isBooked()) {
            return null;
        }

        Optional<SessionEvent> existing =
                sessionEventRepository.findBySessionRequest(sessionRequest.get());

        if (existing.isPresent()
                && !"CANCELLED".equals(existing.get().getEventStatus())) {
            return null;
        }

        int slotsNeeded =
                sessionRequest.get().getSessionDuration() / 30;

        List<TeacherAvailability> bookedSlots =
                checkFront(slot.get(), slotsNeeded);

        if (bookedSlots == null) {
            bookedSlots =
                    checkBothSides(
                            slot.get(),
                            slotsNeeded);
        }

        if (bookedSlots == null) {
            return null;
        }

        for (TeacherAvailability bookedSlot : bookedSlots) {

            bookedSlot.setAvailable(false);
            bookedSlot.setBooked(true);
        }

        Optional<TeacherMeetingDetails> meetingDetails =
                teacherMeetingDetailsRepository.findByTeacherProfile(
                        sessionRequest.get().getTeacherProfile());

        if (meetingDetails.isEmpty()) {
            return null;
        }

        SessionEvent sessionEvent = new SessionEvent();

        sessionEvent.setSessionRequest(sessionRequest.get());

        sessionEvent.setStudentProfile(
                sessionRequest.get().getStudentProfile());

        sessionEvent.setTeacherProfile(
                sessionRequest.get().getTeacherProfile());

        sessionEvent.setStartTime(
                bookedSlots.get(0).getStartTime());

        sessionEvent.setEndTime(
                bookedSlots.get(bookedSlots.size() - 1).getEndTime());

        sessionEvent.setMeetLink(
                meetingDetails.get().getMeetingLink());

        sessionEvent.setEventStatus("UPCOMING");

        sessionEvent.setPaymentAvailable(false);

        sessionRequest.get().setStatus("BOOKED");

        teacherAvailabilityRepository.saveAll(bookedSlots);

        sessionRequestRepository.save(sessionRequest.get());

        return sessionEventRepository.save(sessionEvent);
    }




    @Transactional
    public boolean cancelBooking(
            Long sessionEventId,
            Authentication authentication) {

        Optional<SessionEvent> session =
                sessionEventRepository.findById(sessionEventId);

        if (session.isEmpty()) {
            return false;
        }

        boolean isStudent = isOwner(
                session.get().getStudentProfile().getUser().getUsername(),
                authentication);

        boolean isTeacher = isOwner(
                session.get().getTeacherProfile().getUser().getUsername(),
                authentication);

        if (!isStudent && !isTeacher) {
            return false;
        }

        // Cannot cancel ongoing or completed sessions
        if (!session.get().getStartTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Ongoing or completed sessions cannot be cancelled.");
        }

        session.get().setEventStatus("CANCELLED");
        session.get().getSessionRequest().setStatus("CANCELLED");

        List<TeacherAvailability> bookedSlots =
                teacherAvailabilityRepository
                        .findByTeacherProfileAndBookedTrueOrderByStartTimeAsc(
                                session.get().getTeacherProfile());

        for (TeacherAvailability slot : bookedSlots) {
            if (!slot.getStartTime().isBefore(session.get().getStartTime())
                    && !slot.getEndTime().isAfter(session.get().getEndTime())) {
                slot.setBooked(false);
                slot.setAvailable(true);
            }
        }

        teacherAvailabilityRepository.saveAll(bookedSlots);
        sessionRequestRepository.save(session.get().getSessionRequest());
        sessionEventRepository.save(session.get());

        return true;
    }
}
