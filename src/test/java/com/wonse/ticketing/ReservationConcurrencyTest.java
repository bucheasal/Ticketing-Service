package com.wonse.ticketing;

import com.wonse.domain.reservation.dto.ReservationHoldRequest;
import com.wonse.domain.reservation.repository.ReservationSeatRepository;
import com.wonse.domain.reservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Sql(scripts = {"/db/schema.sql", "/db/data.sql", "/db/concurrency-users.sql"})
public class ReservationConcurrencyTest {
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationSeatRepository reservationSeatRepository;

    @Test
    void 같은좌석에_동시에_선점_요청하면_하나만_성공한다() throws InterruptedException {
        // 같은 좌석에 동시에 100명 선점 요청

        int threadCount = 100;
        Long scheduleId = 1L;
        Long seatId = 1L;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            long userId = i+1L;

            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();

                    reservationService.holdSeats(
                            userId,
                            new ReservationHoldRequest(scheduleId, List.of(seatId))
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            } );
        }
        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);
        assertThat(reservationSeatRepository.findAllBySeatIdIn(List.of(seatId))).hasSize(1);

        executorService.shutdown();
    }

    @Test
    void 서로다른좌석에_동시에_선점_요청하면_모두_성공한다() throws InterruptedException {
        int threadCount = 100;
        Long scheduleId = 1L;

        ConcurrencyResult result = runConcurrently(threadCount, index -> {
            long userId = index + 1L;
            long seatId = index + 1L;

            reservationService.holdSeats(
                    userId,
                    new ReservationHoldRequest(scheduleId, List.of(seatId))
            );
        });

        assertThat(result.successCount()).isEqualTo(100);
        assertThat(result.failCount()).isZero();
        assertThat(reservationSeatRepository.findAllBySeatIdIn(range(1, 100))).hasSize(100);
    }

    @Test
    void 겹치는_다중좌석에_동시에_선점_요청하면_하나만_성공하고_부분선점은_남지않는다() throws InterruptedException {
        Long scheduleId = 1L;

        ConcurrencyResult result = runConcurrently(2, index -> {
            long userId = index + 1L;
            List<Long> seatIds = index == 0 ? List.of(1L, 2L) : List.of(2L, 3L);

            reservationService.holdSeats(
                    userId,
                    new ReservationHoldRequest(scheduleId, seatIds)
            );
        });

        var reservationSeats = reservationSeatRepository.findAllBySeatIdIn(List.of(1L, 2L, 3L));
        var seatTwoReservations = reservationSeatRepository.findAllBySeatIdIn(List.of(2L));

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failCount()).isEqualTo(1);
        assertThat(reservationSeats).hasSize(2);
        assertThat(seatTwoReservations).hasSize(1);
        assertThat(reservationSeats)
                .extracting("reservationId")
                .containsOnly(reservationSeats.get(0).getReservationId());
    }

    @Test
    void 같은사용자가_동시에_여러좌석을_선점해도_최대_두좌석까지만_성공한다() throws InterruptedException {
        Long scheduleId = 1L;
        Long userId = 1L;

        runConcurrently(2, index -> {
            List<Long> seatIds = index == 0 ? List.of(1L, 2L) : List.of(3L);

            reservationService.holdSeats(
                    userId,
                    new ReservationHoldRequest(scheduleId, seatIds)
            );
        });

        assertThat(reservationSeatRepository.findAllBySeatIdIn(List.of(1L, 2L, 3L))).hasSizeLessThanOrEqualTo(2);
    }

    private ConcurrencyResult runConcurrently(int threadCount, ThrowingIntConsumer task) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            int index = i;

            executorService.submit((Callable<Void>) () -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    task.accept(index);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
                return null;
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        return new ConcurrencyResult(successCount.get(), failCount.get());
    }

    private List<Long> range(long startInclusive, long endInclusive) {
        return java.util.stream.LongStream.rangeClosed(startInclusive, endInclusive)
                .boxed()
                .toList();
    }

    @FunctionalInterface
    private interface ThrowingIntConsumer {
        void accept(int value) throws Exception;
    }

    private record ConcurrencyResult(int successCount, int failCount) {
    }
}
