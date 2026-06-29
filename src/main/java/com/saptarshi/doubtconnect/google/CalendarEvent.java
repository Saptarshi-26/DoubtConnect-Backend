package com.saptarshi.doubtconnect.google;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEvent {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
