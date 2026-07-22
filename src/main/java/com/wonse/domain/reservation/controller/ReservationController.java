package com.wonse.domain.reservation.controller;

import com.wonse.domain.reservation.dto.ReservationHoldRequest;
import com.wonse.domain.reservation.dto.ReservationResponse;
import com.wonse.domain.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping("/reservations/holds")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse holdSeats(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ReservationHoldRequest request
    ) {
        return reservationService.holdSeats(userId, request);
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ReservationResponse confirm(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long reservationId
    ) {
        return reservationService.confirm(userId, reservationId);
    }
}
