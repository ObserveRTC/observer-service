DROP DATABASE IF EXISTS WebRTCObserver;
CREATE DATABASE WebRTCObserver;


-- --------------------------------------------------- 
-- ---------- Observer -------------------------------
-- ---------------------------------------------------



DROP TABLE IF EXISTS `WebRTCObserver`.`ActiveStreams`;
CREATE TABLE `WebRTCObserver`.`ActiveStreams`
(
    `serviceUUID`         BINARY(16),
    `SSRC`                BIGINT(8),
    `callUUID`            BINARY(16),
    PRIMARY KEY (`serviceUUID`,`SSRC`),
    INDEX `ActiveStreams_call_index` (`callUUID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='A table to track the active streams';
 

DROP TABLE IF EXISTS `WebRTCObserver`.`PeerConnections`;
CREATE TABLE `WebRTCObserver`.`PeerConnections`
(
    `peerConnectionUUID`  BINARY(16),
    `callUUID`            BINARY(16),
    `serviceUUID`         BINARY(16),
    `joined`              BIGINT(8),
    `updated`             BIGINT(8),
    `detached`            BIGINT(8),
    `mediaUnitID`         VARCHAR (255),
    `browserID`           VARCHAR (255),
    `providedUserID`      VARCHAR (255),
    `providedCallID`      VARCHAR (255),
    `timeZone`            VARCHAR (255),
    `serviceName`         VARCHAR (255),
    PRIMARY KEY (`peerConnectionUUID`),
    INDEX `PeerConnections_updated_index` (`updated`),
    INDEX `PeerConnections_detached_index` (`detached`),
    INDEX `PeerConnections_browserID_index` (`browserID`),
    INDEX `PeerConnections_providedCallID_index` (`providedCallID`),
    INDEX `PeerConnections_callUUID_index` (`callUUID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='A table to store information related to peer connections';
  
DROP TABLE IF EXISTS `WebRTCObserver`.`SentReports`;
CREATE TABLE `WebRTCObserver`.`SentReports`
(
    `signature`             VARBINARY(255),
    `peerConnectionUUID`    BINARY(16),
    `reported`              BIGINT(8),
    PRIMARY KEY (`signature`),
    INDEX `SentReports_peerConnectionUUID_index` (`peerConnectionUUID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='SentReports';
  
  
  
-- --------------------------------------------------- 
-- ---------- Authenticator --------------------------
-- ---------------------------------------------------




CREATE DATABASE IF NOT EXISTS WebRTCObserver;

DROP TABLE IF EXISTS `WebRTCObserver`.`Users`;
CREATE TABLE `WebRTCObserver`.`Users`
(
    `id`              INT  NOT NULL AUTO_INCREMENT COMMENT 'The identifier of the user for inside relations, never outside',
    `uuid`            BINARY(16) UNIQUE   DEFAULT NULL COMMENT 'The UUID of the user published outside ',
    `username`        VARCHAR(255) UNIQUE DEFAULT NULL COMMENT 'The username of the user',
    `password_digest` BINARY(64)          DEFAULT NULL COMMENT 'The hash of the password using the salt',
    `password_salt`   BINARY(32)          DEFAULT NULL COMMENT 'The salt for the password',
    `role`            ENUM ('admin','client') NOT NULL COMMENT 'The role of the user determines of which endpoint it can access to',
    PRIMARY KEY (`id`),
    KEY `users_username_key` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Users';

DROP TABLE IF EXISTS `WebRTCObserver`.`Customers`;
CREATE TABLE `WebRTCObserver`.`Customers`
(
    `id`          INT NOT NULL AUTO_INCREMENT,
    `uuid`        BINARY(16) UNIQUE DEFAULT NULL,
    `name`        VARCHAR(255)      DEFAULT NULL,
    `description` VARCHAR(255)      DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `customers_uuid_key` (`uuid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Customers';
  

DROP TABLE IF EXISTS `WebRTCObserver`.`Services`;
CREATE TABLE `WebRTCObserver`.`Services`
(
    `id`          INT NOT NULL AUTO_INCREMENT,
    `customer_id` INT NOT NULL,
    `uuid`        BINARY(16) UNIQUE DEFAULT NULL,
    `name`        VARCHAR(255)      DEFAULT NULL,
    `description` VARCHAR(255)      DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `services_uuid_key` (`uuid`),
    FOREIGN KEY (customer_id) REFERENCES Customers(id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Services';
  

DROP TABLE IF EXISTS `WebRTCObserver`.`Bridges`;
CREATE TABLE `WebRTCObserver`.`Bridges`
(
    `id`          INT NOT NULL AUTO_INCREMENT,
    `service_id`  INT NOT NULL,
    `name`        VARCHAR(255)      DEFAULT NULL,
    `description` VARCHAR(255)      DEFAULT NULL,
     PRIMARY KEY (`id`),
     FOREIGN KEY (service_id) REFERENCES Services(id),
     UNIQUE `bridges_unique_index`(`service_id`, `name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Services';

USE `WebRTCObserver`;
INSERT INTO `Users`
(`id`,
 `uuid`,
 `username`,
 `password_digest`,
 `password_salt`,
 `role`)
VALUES (1,
        UNHEX(REPLACE('58a6314b-188c-4659-a046-553a7f8c96de', '-', '')),
        'balazs',
        UNHEX('e77183b020e803e858c39b95652c81084f19ed11e2e2d18433bcb2c8a8a46768'),
        UNHEX('e12'),
        'admin');
  