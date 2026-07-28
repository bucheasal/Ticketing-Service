SHOW GLOBAL STATUS
WHERE Variable_name IN (
    'Com_commit',
    'Com_rollback',
    'Innodb_deadlocks',
    'Innodb_row_lock_current_waits',
    'Innodb_row_lock_time',
    'Innodb_row_lock_waits'
);
