INSERT INTO `users` (`id`) VALUES
    (1),
    (2),
    (3);

INSERT INTO `event` (`id`, `event_name`, `place`) VALUES
    (1, 'Spring Concert', 'Olympic Hall'),
    (2, 'Java Live', 'Blue Square');

INSERT INTO `event_schedule` (`id`, `event_id`, `start_at`) VALUES
    (1, 1, '2026-08-01 19:00:00'),
    (2, 1, '2026-08-02 19:00:00'),
    (3, 2, '2026-08-03 19:00:00'),
    (4, 2, '2026-08-04 19:00:00');

INSERT INTO `seat` (`schedule_id`, `seat_number`, `status`)
SELECT
    schedules.schedule_id,
    CONCAT('A', LPAD(numbers.number, 3, '0')),
    'EMPTY'
FROM (
    SELECT 1 AS schedule_id
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
) schedules
CROSS JOIN (
    SELECT ones.digit + tens.digit * 10 + 1 AS number
    FROM (
        SELECT 0 AS digit
        UNION ALL SELECT 1
        UNION ALL SELECT 2
        UNION ALL SELECT 3
        UNION ALL SELECT 4
        UNION ALL SELECT 5
        UNION ALL SELECT 6
        UNION ALL SELECT 7
        UNION ALL SELECT 8
        UNION ALL SELECT 9
    ) ones
    CROSS JOIN (
        SELECT 0 AS digit
        UNION ALL SELECT 1
        UNION ALL SELECT 2
        UNION ALL SELECT 3
        UNION ALL SELECT 4
        UNION ALL SELECT 5
        UNION ALL SELECT 6
        UNION ALL SELECT 7
        UNION ALL SELECT 8
        UNION ALL SELECT 9
    ) tens
) numbers
ORDER BY schedules.schedule_id, numbers.number;
