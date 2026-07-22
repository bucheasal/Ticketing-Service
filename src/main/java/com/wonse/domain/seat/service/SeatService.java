package com.wonse.domain.seat.service;

import com.wonse.domain.seat.dto.SeatResponse;
import com.wonse.domain.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatRepository seatRepository;

    public List<SeatResponse> getSeats(Long scheduleId) {
        return seatRepository.findAllByScheduleId(scheduleId)
                .stream()
                .map(seat -> new SeatResponse(
                        seat.getId(),
                        seat.getScheduleId(),
                        seat.getSeatNumber(),
                        seat.getStatus()
                ))
                .toList();
    }
}
