package com.wonse.domain.event.dto;

public record EventResponse(
        Long eventId,
        String eventName,
        String place
) {
}
