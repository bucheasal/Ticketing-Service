package com.wonse.domain.event.service;

import com.wonse.domain.event.dto.EventResponse;
import com.wonse.domain.event.dto.EventScheduleResponse;
import com.wonse.domain.event.repository.EventRepository;
import com.wonse.domain.event.repository.EventScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService{
    private final EventRepository eventRepository;
    private final EventScheduleRepository eventScheduleRepository;

    public List<EventResponse> getEvents() {
        return eventRepository.findAll().stream()
                .map(event -> new EventResponse(
                        event.getId(),
                        event.getEventName(),
                        event.getPlace()
                ))
                .toList();
    }

    public List<EventScheduleResponse> getSchedules(Long eventId) {
        return eventScheduleRepository.findAllByEventId(eventId)
                .stream()
                .map(schedule -> new EventScheduleResponse(
                        schedule.getId(),
                        schedule.getEventId(),
                        schedule.getStartAt()
                ))
                .toList();
    }
}
