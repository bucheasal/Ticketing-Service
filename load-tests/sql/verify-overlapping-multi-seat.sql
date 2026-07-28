SELECT
    COUNT(DISTINCT reservation_id) AS reservation_count,
    COUNT(*) AS reserved_seat_count
FROM reservation_seat
WHERE seat_id IN (1, 2, 3);

SELECT reservation_id, COUNT(*) AS seat_count
FROM reservation_seat
GROUP BY reservation_id
HAVING COUNT(*) <> 2;

SELECT seat_id, COUNT(*) AS duplicate_count
FROM reservation_seat
GROUP BY seat_id
HAVING COUNT(*) > 1;

-- Expected:
-- reservation_count = 1
-- reserved_seat_count = 2
-- partial reservation query returns no rows
-- duplicate query returns no rows
