package com.wonse.domain.event.dto;

import java.time.LocalDateTime;

public record EventScheduleResponse(
        Long scheduleId,
        Long eventId,
        LocalDateTime startAt
) {
}
