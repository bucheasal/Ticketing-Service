package com.wonse.domain.event.repository;

import com.wonse.domain.event.entity.EventSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventScheduleRepository extends JpaRepository<EventSchedule, Long> {
    List<EventSchedule> findAllByEventId(Long eventId);
}
