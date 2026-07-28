SELECT
    COUNT(DISTINCT reservation_id) AS reservation_count,
    COUNT(*) AS reserved_seat_count
FROM reservation_seat
WHERE seat_id = 1;

SELECT seat_id, COUNT(*) AS duplicate_count
FROM reservation_seat
GROUP BY seat_id
HAVING COUNT(*) > 1;

-- Expected:
-- reservation_count = 1
-- reserved_seat_count = 1
-- duplicate query returns no rows
