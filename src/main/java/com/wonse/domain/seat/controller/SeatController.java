package com.wonse.domain.seat.controller;

import com.wonse.domain.seat.dto.SeatResponse;
import com.wonse.domain.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SeatController {
    private final SeatService seatService;

    @GetMapping("/schedules/{scheduleId}/seats")
    public List<SeatResponse> getSeats(@PathVariable Long scheduleId) {
        return seatService.getSeats(scheduleId);
    }
}
