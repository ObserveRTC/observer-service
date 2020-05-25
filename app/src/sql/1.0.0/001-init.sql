CREATE DATABASE IF NOT EXISTS ObserveRTC;

CREATE TABLE `Users`
(
    `id`              INT                               NOT NULL AUTO_INCREMENT COMMENT 'The identifier of the user for inside relations, never outside',
    `uuid`            BINARY(16) UNIQUE   DEFAULT NULL COMMENT 'The UUID of the user published outside ',
    `username`        VARCHAR(255) UNIQUE DEFAULT NULL COMMENT 'The username of the user',
    `password_digest` BINARY(64)          DEFAULT NULL COMMENT 'The hash of the password using the salt',
    `password_salt`   BINARY(32)          DEFAULT NULL COMMENT 'The salt for the password',
    `role`            ENUM ('customer','administrator') NOT NULL COMMENT 'The role of the user determines of which endpoint it can access to',
    PRIMARY KEY (`id`),
    KEY `users_username_key` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Users';

CREATE TABLE `Organisations`
(
    `id`          INT NOT NULL AUTO_INCREMENT COMMENT 'The identifier of the organization for inside relations, never outside',
    `uuid`        BINARY(16) UNIQUE   DEFAULT NULL COMMENT 'The UUID of the organization published outside ',
    `name`        VARCHAR(255) UNIQUE DEFAULT NULL COMMENT 'The name of the organization',
    `description` VARCHAR(255)        DEFAULT NULL COMMENT 'The description for the organization',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Organizations';


CREATE TABLE `Observers`
(
    `id`          INT NOT NULL AUTO_INCREMENT COMMENT 'The identifier of the observer for inside relations, never outside',
    `uuid`        BINARY(16) UNIQUE DEFAULT NULL COMMENT 'The UUID of the observer published outside ',
    `name`        VARCHAR(255)      DEFAULT NULL COMMENT 'The name of the obersver',
    `description` VARCHAR(255)      DEFAULT NULL COMMENT 'The description for the observer',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Observers';

CREATE TABLE `Evaluators`
(
    `id`          INT NOT NULL AUTO_INCREMENT COMMENT 'The identifier of the observer for inside relations, never outside',
    `uuid`        BINARY(16) UNIQUE   DEFAULT NULL COMMENT 'The UUID of the observer published outside ',
    `name`        VARCHAR(255) UNIQUE DEFAULT NULL COMMENT 'The name of the obersver',
    `description` VARCHAR(255)        DEFAULT NULL COMMENT 'The description for the observer',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Evaluators';

CREATE TABLE `ObserverEvaluators`
(
    `observer_id`  INT NOT NULL COMMENT 'The identifier of the observer for inside relations, never outside',
    `evaluator_id` INT NOT NULL COMMENT 'The UUID of the observer published outside ',
    FOREIGN KEY (observer_id)
        REFERENCES Observers (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (evaluator_id)
        REFERENCES Evaluators (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='An associative table to map Observers to Evaluators';


