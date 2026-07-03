package com.saptarshi.doubtconnect.controller.sessions;

import com.saptarshi.doubtconnect.dto.SessionEventResponseDto;
import com.saptarshi.doubtconnect.service.SessionEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/session-event")
public class SessionEventController {

    @Autowired
    private SessionEventService service;


    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<SessionEventResponseDto> dto =
                service.getSessionEventById(
                        id,
                        authentication);

        return dto.isPresent()
                ? new ResponseEntity<>(dto.get(), HttpStatus.OK)
                : new ResponseEntity<>(
                "Session Event Not Found",
                HttpStatus.NOT_FOUND);
    }

    @GetMapping("/student/{id}")
    public ResponseEntity<?> getStudentSessions(
            @PathVariable long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.getStudentSessions(id, authentication));
    }

    @GetMapping("/teacher/{id}")
    public ResponseEntity<?> getTeacherSessions(
            @PathVariable long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.getTeacherSessions(id, authentication));
    }

    @GetMapping("/student/upcoming/{id}")
    public ResponseEntity<?> getUpcomingStudentSessions(
            @PathVariable long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.getUpcomingStudentSessions(id, authentication));
    }

    @GetMapping("/teacher/upcoming/{id}")
    public ResponseEntity<?> getUpcomingTeacherSessions(
            @PathVariable long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.getUpcomingTeacherSessions(id, authentication));
    }
}
