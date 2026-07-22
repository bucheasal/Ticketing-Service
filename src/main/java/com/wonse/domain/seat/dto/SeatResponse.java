package com.wonse.domain.seat.dto;

import com.wonse.domain.seat.entity.SeatStatus;

public record SeatResponse(
        Long seatId,
        Long scheduleId,
        String seatNumber,
        SeatStatus status
) {
}
