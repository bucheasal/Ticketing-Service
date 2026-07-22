package com.wonse.domain.reservation.service;

import com.wonse.domain.event.repository.EventScheduleRepository;
import com.wonse.domain.reservation.dto.ReservationHoldRequest;
import com.wonse.domain.reservation.dto.ReservationResponse;
import com.wonse.domain.reservation.entity.Reservation;
import com.wonse.domain.reservation.entity.ReservationSeat;
import com.wonse.domain.reservation.entity.ReservationStatus;
import com.wonse.domain.reservation.repository.ReservationRepository;
import com.wonse.domain.reservation.repository.ReservationSeatRepository;
import com.wonse.domain.seat.entity.Seat;
import com.wonse.domain.seat.repository.SeatRepository;
import com.wonse.domain.user.entity.User;
import com.wonse.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private static final int HOLD_MINUTES = 5;
    private static final int MAX_SEATS_PER_USER = 2;
    private static final List<ReservationStatus> ACTIVE_STATUSES = List.of(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED
    );

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final SeatRepository seatRepository;
    private final EventScheduleRepository eventScheduleRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReservationResponse holdSeats(Long userId, ReservationHoldRequest request) {
        validateHoldRequest(request);
        ensureUserForUpdate(userId);
        ensureSchedule(request.scheduleId());

        LocalDateTime now = LocalDateTime.now();
        List<Long> seatIds = request.seatIds().stream().distinct().toList();
        List<Seat> seats = seatRepository.findAllByScheduleIdAndIdInForUpdate(request.scheduleId(), seatIds);
        validateRequestedSeats(request.scheduleId(), seatIds, seats);
        releaseExpiredHolds(seatIds, now);
        validateUserSeatLimit(userId, request.scheduleId(), seatIds.size());

        seats.forEach(Seat::hold);
        Reservation reservation = reservationRepository.save(new Reservation(userId, request.scheduleId(), now));

        try {
            List<ReservationSeat> reservationSeats = seats.stream()
                    .map(seat -> new ReservationSeat(seat.getId(), seat.getScheduleId(), reservation.getId()))
                    .toList();
            reservationSeatRepository.saveAll(reservationSeats);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("이미 선점되었거나 확정된 좌석이 포함되어 있습니다.", exception);
        }

        return toResponse(reservation, seatIds);
    }

    @Transactional
    public ReservationResponse confirm(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 예약만 확정할 수 있습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (reservation.isExpired(now)) {
            expireReservation(reservation);
            throw new IllegalArgumentException("선점 시간이 만료되었습니다.");
        }

        reservation.confirm();
        List<Long> seatIds = reservationSeatRepository.findAllByReservationId(reservationId)
                .stream()
                .map(ReservationSeat::getSeatId)
                .toList();

        return toResponse(reservation, seatIds);
    }

    private void validateHoldRequest(ReservationHoldRequest request) {
        List<Long> distinctSeatIds = request.seatIds().stream().distinct().toList();
        if (distinctSeatIds.size() != request.seatIds().size()) {
            throw new IllegalArgumentException("중복된 좌석 ID가 포함되어 있습니다.");
        }
    }

    private void ensureUserForUpdate(Long userId) {
        if (userRepository.findByIdForUpdate(userId).isEmpty()) {
            userRepository.saveAndFlush(new User(userId));
        }
    }

    private void ensureSchedule(Long scheduleId) {
        if (!eventScheduleRepository.existsById(scheduleId)) {
            throw new IllegalArgumentException("회차를 찾을 수 없습니다.");
        }
    }

    private void validateRequestedSeats(Long scheduleId, List<Long> seatIds, List<Seat> seats) {
        if (seats.size() != seatIds.size()) {
            throw new IllegalArgumentException("요청한 좌석 중 존재하지 않는 좌석이 있습니다.");
        }

        boolean hasDifferentSchedule = seats.stream()
                .anyMatch(seat -> !seat.getScheduleId().equals(scheduleId));
        if (hasDifferentSchedule) {
            throw new IllegalArgumentException("요청한 회차에 속하지 않는 좌석이 포함되어 있습니다.");
        }
    }

    private void releaseExpiredHolds(List<Long> seatIds, LocalDateTime now) {
        List<ReservationSeat> reservationSeats = reservationSeatRepository.findAllBySeatIdIn(seatIds);
        Map<Long, Reservation> reservations = reservationRepository.findAllById(
                        reservationSeats.stream()
                                .map(ReservationSeat::getReservationId)
                                .collect(Collectors.toSet())
                )
                .stream()
                .collect(Collectors.toMap(Reservation::getId, Function.identity()));

        for (ReservationSeat reservationSeat : reservationSeats) {
            Reservation reservation = reservations.get(reservationSeat.getReservationId());
            if (reservation == null) {
                continue;
            }

            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                throw new IllegalArgumentException("이미 확정된 좌석이 포함되어 있습니다.");
            }

            if (!reservation.isExpired(now)) {
                throw new IllegalArgumentException("이미 선점된 좌석이 포함되어 있습니다.");
            }

            reservation.expire();
            reservationSeatRepository.delete(reservationSeat);
            seatRepository.findById(reservationSeat.getSeatId()).ifPresent(Seat::release);
        }
    }

    private void validateUserSeatLimit(Long userId, Long scheduleId, int requestedSeatCount) {
        List<Reservation> activeReservations = reservationRepository.findAllByUserIdAndScheduleIdAndStatusIn(
                userId,
                scheduleId,
                ACTIVE_STATUSES
        );
        Set<Long> activeReservationIds = activeReservations.stream()
                .map(Reservation::getId)
                .collect(Collectors.toSet());
        int activeSeatCount = reservationSeatRepository.findAllByReservationIdIn(activeReservationIds).size();

        if (activeSeatCount + requestedSeatCount > MAX_SEATS_PER_USER) {
            throw new IllegalArgumentException("사용자당 최대 2개 좌석만 예약할 수 있습니다.");
        }
    }

    private void expireReservation(Reservation reservation) {
        reservation.expire();
        List<ReservationSeat> reservationSeats = reservationSeatRepository.findAllByReservationId(reservation.getId());
        reservationSeatRepository.deleteAll(reservationSeats);
        reservationSeats.forEach(reservationSeat ->
                seatRepository.findById(reservationSeat.getSeatId()).ifPresent(Seat::release)
        );
    }

    private ReservationResponse toResponse(Reservation reservation, List<Long> seatIds) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getScheduleId(),
                reservation.getStatus(),
                reservation.getPreemptTime(),
                reservation.getPreemptTime().plusMinutes(HOLD_MINUTES),
                seatIds
        );
    }
}
