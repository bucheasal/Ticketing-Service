package com.wonse.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservation",
        indexes = {
                @Index(name = "IDX_RESERVATION_USER_STATUS", columnList = "user_id, status"),
                @Index(name = "IDX_RESERVATION_SCHEDULE_STATUS", columnList = "schedule_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ReservationStatus status;

    @Column(name = "preempt_time")
    private LocalDateTime preemptTime;

    public Reservation(Long userId, Long scheduleId, LocalDateTime preemptTime) {
        this.userId = userId;
        this.scheduleId = scheduleId;
        this.status = ReservationStatus.PENDING;
        this.preemptTime = preemptTime;
    }

    public void confirm() {
        if (this.status != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("PENDING 상태의 예약만 확정할 수 있습니다");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void expire() {
        if (this.status == ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("확정 완료는 만료할 수 없다");
        }
        this.status = ReservationStatus.EXPIRED;
    }

    public boolean isExpired(LocalDateTime now) {
        return this.status == ReservationStatus.PENDING && this.preemptTime.plusMinutes(5).isBefore(now);
    }
}
