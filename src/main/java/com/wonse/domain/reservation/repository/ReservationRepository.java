package com.wonse.domain.reservation.repository;

import com.wonse.domain.reservation.entity.Reservation;
import com.wonse.domain.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByUserIdAndStatusIn(Long userId, Collection<ReservationStatus> statuses);

    List<Reservation> findAllByUserIdAndScheduleIdAndStatusIn(
            Long userId,
            Long scheduleId,
            Collection<ReservationStatus> statuses
    );

    List<Reservation> findAllByScheduleIdAndStatus(Long scheduleId, ReservationStatus status);

    long countByUserIdAndStatusIn(Long userId, Collection<ReservationStatus> statuses);
}
