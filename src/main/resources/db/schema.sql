DROP TABLE IF EXISTS `reservation_seat`;
DROP TABLE IF EXISTS `reservation`;
DROP TABLE IF EXISTS `seat`;
DROP TABLE IF EXISTS `event_schedule`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `event`;

CREATE TABLE `event` (
                         `id`	bigint	NOT NULL AUTO_INCREMENT,
                         `event_name`	varchar(100)	NOT NULL,
                         `place`	varchar(100)	NOT NULL,
                         PRIMARY KEY (id)
);

CREATE TABLE `reservation` (
                               `id`	bigint	NOT NULL AUTO_INCREMENT,
                               `user_id`	bigint	NOT NULL,
                               `schedule_id`	bigint	NOT NULL,
                               `status`	varchar(10)	NOT NULL	COMMENT 'PENDING, CONFIRMED, EXPIRED, CANCELLED',
                               `preempt_time`	datetime	NULL,
                               PRIMARY KEY (`id`),
                               INDEX `IDX_RESERVATION_USER_STATUS` (`user_id`, `status`),
                               INDEX `IDX_RESERVATION_SCHEDULE_STATUS` (`schedule_id`, `status`)
);

CREATE TABLE `users` (
                         `id`	bigint	NOT NULL AUTO_INCREMENT,
                         PRIMARY KEY (id)
);

CREATE TABLE `event_schedule` (
                                  `id`	bigint	NOT NULL AUTO_INCREMENT,
                                  `event_id`	bigint	NOT NULL,
                                  `start_at`	datetime	NOT NULL,
                                  PRIMARY KEY (id)
);

CREATE TABLE `seat` (
                        `id`	bigint	NOT NULL AUTO_INCREMENT,
                        `schedule_id`	bigint	NOT NULL,
                        `seat_number`	varchar(10)	NOT NULL,
                        `status`	varchar(10)	NOT NULL	COMMENT 'HELD, EMPTY',
                        PRIMARY KEY (id),
                        UNIQUE (`schedule_id`, `seat_number`)
);

CREATE TABLE `reservation_seat` (
                                    `id`	bigint	NOT NULL AUTO_INCREMENT,
                                    `seat_id`	bigint	NOT NULL,
                                    `schedule_id`	bigint	NOT NULL,
                                    `reservation_id`	bigint	NOT NULL,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `UK_RESERVATION_SEAT_SEAT` (`seat_id`),
                                    INDEX `IDX_RESERVATION_SEAT_RESERVATION` (`reservation_id`)
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
                                                                                           `seat_id`
    )
    REFERENCES `seat` (
                       `id`
        );

ALTER TABLE `reservation_seat` ADD CONSTRAINT `FK_reservation_TO_reservation_seat_1` FOREIGN KEY (
                                                                                                  `reservation_id`
    )
    REFERENCES `reservation` (
                              `id`
        );
