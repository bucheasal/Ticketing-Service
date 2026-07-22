package com.wonse.domain.reservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReservationHoldRequest(
        @NotNull
        Long scheduleId,

        @NotEmpty
        @Size(max = 2)
        List<Long> seatIds
) {
}
