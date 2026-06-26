package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.service.SessionEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/session-event")
public class SessionEventController {

    @Autowired
    private SessionEventService service;

    @GetMapping("/student/{id}")
    public ResponseEntity<List<SessionEvent>> getStudentSessions(
            @PathVariable long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.getStudentSessions(id, authentication));
    }

    @GetMapping("/teacher/{id}")
    public ResponseEntity<List<SessionEvent>> getTeacherSessions(
            @PathVariable long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.getTeacherSessions(id, authentication));
    }

    @GetMapping("/student/upcoming/{id}")
    public ResponseEntity<List<SessionEvent>> getUpcomingStudentSessions(
            @PathVariable long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.getUpcomingStudentSessions(id, authentication));
    }

    @GetMapping("/teacher/upcoming/{id}")
    public ResponseEntity<List<SessionEvent>> getUpcomingTeacherSessions(
            @PathVariable long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.getUpcomingTeacherSessions(id, authentication));
    }
}
