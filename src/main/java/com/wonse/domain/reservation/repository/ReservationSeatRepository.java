package com.wonse.domain.reservation.repository;

import com.wonse.domain.reservation.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {
    List<ReservationSeat> findAllByReservationId(Long reservationId);

    List<ReservationSeat> findAllByReservationIdIn(Collection<Long> reservationIds);

    List<ReservationSeat> findAllBySeatIdIn(Collection<Long> seatIds);

    Optional<ReservationSeat> findBySeatId(Long seatId);

    boolean existsBySeatId(Long seatId);
}
