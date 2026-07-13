DROP TABLE IF EXISTS `reservation_seat`;
DROP TABLE IF EXISTS `reservation`;
DROP TABLE IF EXISTS `seat`;
DROP TABLE IF EXISTS `event_schedule`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `event`;

CREATE TABLE `event` (
                         `id`	bigint	NOT NULL,
                         `event_name`	varchar(100)	NOT NULL,
                         `place`	varchar(100)	NOT NULL
);

CREATE TABLE `reservation` (
                               `id`	bigint	NOT NULL,
                               `user_id`	bigint	NOT NULL,
                               `schedule_id`	bigint	NOT NULL,
                               `status`	varchar(10)	NOT NULL	COMMENT 'PENDING, CONFIRMED, EXPIRED, CANCELLED',
                               `preempt_time`	datetime	NULL
);

CREATE TABLE `users` (
                         `id`	bigint	NOT NULL,
                         `password`	varchar(255)	NOT NULL
);

CREATE TABLE `event_schedule` (
                                  `id`	bigint	NOT NULL,
                                  `event_id`	bigint	NOT NULL,
                                  `start_at`	datetime	NOT NULL
);

CREATE TABLE `seat` (
                        `id`	bigint	NOT NULL,
                        `schedule_id`	bigint	NOT NULL,
                        `seat_number`	varchar(10)	NOT NULL,
                        `status`	varchar(10)	NOT NULL	COMMENT 'HELD, EMPTY'
);

CREATE TABLE `reservation_seat` (
                                    `id`	bigint	NOT NULL,
                                    `seat_id`	bigint	NOT NULL,
                                    `schedule_id`	bigint	NOT NULL,
                                    `reservation_id`	bigint	NOT NULL
);

ALTER TABLE `event` ADD CONSTRAINT `PK_EVENT` PRIMARY KEY (
                                                           `id`
    );

ALTER TABLE `reservation` ADD CONSTRAINT `PK_RESERVATION` PRIMARY KEY (
                                                                       `id`
    );

ALTER TABLE `users` ADD CONSTRAINT `PK_USERS` PRIMARY KEY (
                                                           `id`
    );

ALTER TABLE `event_schedule` ADD CONSTRAINT `PK_EVENT_SCHEDULE` PRIMARY KEY (
                                                                             `id`
    );

ALTER TABLE `seat` ADD CONSTRAINT `PK_SEAT` PRIMARY KEY (
                                                         `id`,
                                                         `schedule_id`
    );

ALTER TABLE `reservation_seat` ADD CONSTRAINT `PK_RESERVATION_SEAT` PRIMARY KEY (
                                                                                 `id`
    );

ALTER TABLE `reservation` ADD CONSTRAINT `FK_users_TO_reservation_1` FOREIGN KEY (
                                                                                  `user_id`
    )
    REFERENCES `users` (
                        `id`
        );

ALTER TABLE `reservation` ADD CONSTRAINT `FK_event_schedule_TO_reservation_1` FOREIGN KEY (
                                                                                           `schedule_id`
    )
    REFERENCES `event_schedule` (
                                 `id`
        );

ALTER TABLE `event_schedule` ADD CONSTRAINT `FK_event_TO_event_schedule_1` FOREIGN KEY (
                                                                                        `event_id`
    )
    REFERENCES `event` (
                        `id`
        );

ALTER TABLE `seat` ADD CONSTRAINT `FK_event_schedule_TO_seat_1` FOREIGN KEY (
                                                                             `schedule_id`
    )
    REFERENCES `event_schedule` (
                                 `id`
        );

ALTER TABLE `reservation_seat` ADD CONSTRAINT `FK_seat_TO_reservation_seat_1` FOREIGN KEY (
                                                                                           `seat_id`,
                                                                                         `schedule_id`
    )
    REFERENCES `seat` (
                       `id`,
                       `schedule_id`
        );

ALTER TABLE `reservation_seat` ADD CONSTRAINT `FK_reservation_TO_reservation_seat_1` FOREIGN KEY (
                                                                                                  `reservation_id`
    )
    REFERENCES `reservation` (
                              `id`
        );
ALTER TABLE `reservation_seat` ADD CONSTRAINT `UK_RESERVATION_SEAT` UNIQUE (
    `seat_id`,
    `schedule_id`
    );
