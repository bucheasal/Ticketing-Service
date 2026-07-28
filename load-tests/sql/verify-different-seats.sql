SELECT
    COUNT(DISTINCT reservation_id) AS reservation_count,
    COUNT(DISTINCT seat_id) AS distinct_reserved_seat_count,
    COUNT(*) AS reserved_seat_count
FROM reservation_seat
WHERE seat_id BETWEEN 1 AND 100;

SELECT seat_id, COUNT(*) AS duplicate_count
FROM reservation_seat
GROUP BY seat_id
HAVING COUNT(*) > 1;

-- Expected:
-- reservation_count = 100
-- distinct_reserved_seat_count = 100
-- reserved_seat_count = 100
-- duplicate query returns no rows
