package com.wonse.domain.seat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "seat",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_SEAT_SCHEDULE_SEAT_NUMBER",
                        columnNames = {"schedule_id", "seat_number"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private SeatStatus status;

    public void hold() {
        if (this.status != SeatStatus.EMPTY) {
            throw new IllegalArgumentException("이미 선점된 좌석입니다.");
        }
        this.status = SeatStatus.HELD;
    }

    public void release() {
        this.status = SeatStatus.EMPTY;
    }
}
