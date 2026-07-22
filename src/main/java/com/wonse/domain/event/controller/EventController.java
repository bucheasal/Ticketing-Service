package com.wonse.domain.event.controller;

import com.wonse.domain.event.dto.EventResponse;
import com.wonse.domain.event.dto.EventScheduleResponse;
import com.wonse.domain.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping("/events")
    public List<EventResponse> getEvents() {
        return eventService.getEvents();
    }

    @GetMapping("/events/{eventId}/schedules")
    public List<EventScheduleResponse> getSchedules(@PathVariable Long eventId) {
        return eventService.getSchedules(eventId);
    }
}
