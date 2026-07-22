package com.wonse.domain.reservation.dto;

import com.wonse.domain.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ReservationResponse(
        Long reservationId,
        Long userId,
        Long scheduleId,
        ReservationStatus status,
        LocalDateTime preemptTime,
        LocalDateTime expiresAt,
        List<Long> seatIds
) {
}
