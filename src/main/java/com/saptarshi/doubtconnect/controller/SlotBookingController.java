package com.saptarshi.doubtconnect.controller;

import com.saptarshi.doubtconnect.entity.SessionEvent;
import com.saptarshi.doubtconnect.service.SlotBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/slot-booking")
public class SlotBookingController {

    @Autowired
    private SlotBookingService slotBookingService;

    @PostMapping("/book")
    public ResponseEntity<SessionEvent> bookSlot(
            @RequestParam Long sessionRequestId,
            @RequestParam Long slotId,
            Authentication authentication) {

        SessionEvent sessionEvent =
                slotBookingService.bookSlot(
                        sessionRequestId,
                        slotId,
                        authentication);

        if (sessionEvent == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(sessionEvent);
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<Void> cancelBooking(
            @RequestParam Long sessionEventId,
            Authentication authentication) {

        if (!slotBookingService.cancelBooking(
                sessionEventId,
                authentication)) {

            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }
}