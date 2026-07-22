package com.wonse.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "reservation_seat",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_RESERVATION_SEAT_SEAT", columnNames = "seat_id")
        },
        indexes = {
                @Index(name = "IDX_RESERVATION_SEAT_RESERVATION", columnList = "reservation_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReservationSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    public ReservationSeat(Long seatId, Long scheduleId, Long reservationId) {
        this.seatId = seatId;
        this.scheduleId = scheduleId;
        this.reservationId = reservationId;
    }
}
