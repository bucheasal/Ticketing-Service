package com.wonse.domain.seat.repository;

import com.wonse.domain.seat.entity.Seat;
import com.wonse.domain.seat.entity.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findAllByScheduleId(Long scheduleId);

    List<Seat> findAllByScheduleIdAndIdIn(Long scheduleId, Collection<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.scheduleId = :scheduleId and s.id in :seatIds")
    List<Seat> findAllByScheduleIdAndIdInForUpdate(
            @Param("scheduleId") Long scheduleId,
            @Param("seatIds") Collection<Long> seatIds
    );

    Optional<Seat> findByScheduleIdAndSeatNumber(Long scheduleId, String seatNumber);

    long countByScheduleIdAndStatus(Long scheduleId, SeatStatus status);
}
