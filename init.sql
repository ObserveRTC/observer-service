
CREATE DATABASE IF NOT EXISTS WebRTCObserver;

CREATE TABLE `WebRTCObserver`.`Users`
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

CREATE TABLE `WebRTCObserver`.`Organisations`
(
    `id`          INT NOT NULL AUTO_INCREMENT COMMENT 'The identifier of the organization for inside relations, never outside',
    `uuid`        BINARY(16) UNIQUE   DEFAULT NULL COMMENT 'The UUID of the organization published outside ',
    `name`        VARCHAR(255) UNIQUE DEFAULT NULL COMMENT 'The name of the organization',
    `description` VARCHAR(255)        DEFAULT NULL COMMENT 'The description for the organization',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Organizations';


CREATE TABLE `WebRTCObserver`.`Observers`
(
    `id`          INT NOT NULL AUTO_INCREMENT COMMENT 'The identifier of the observer for inside relations, never outside',
    `uuid`        BINARY(16) UNIQUE DEFAULT NULL COMMENT 'The UUID of the observer published outside ',
    `name`        VARCHAR(255)      DEFAULT NULL COMMENT 'The name of the obersver',
    `description` VARCHAR(255)      DEFAULT NULL COMMENT 'The description for the observer',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Observers';

CREATE TABLE `WebRTCObserver`.`ObserverOrganisations`
(
    `observer_id`  INT NOT NULL COMMENT 'The identifier of the observer for inside relations, never outside',
    `organisation_id` INT NOT NULL COMMENT 'The identifier of the organisations for inside relations, never outside',
    FOREIGN KEY (observer_id)
        REFERENCES Observers (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (organisation_id)
        REFERENCES Organisations (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='An associative table to map Observers to Evaluators';



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
        'administrator');

INSERT INTO `Observers`
(`id`,
 `uuid`,
 `name`,
 `description`)
VALUES (1,
        UNHEX(REPLACE('86ed98c6-b001-48bb-b31e-da638b979c72', '-', '')),
        'demo',
        'demo description');


INSERT INTO `Organisations`
(`id`,
 `uuid`,
 `name`,
 `description`)
VALUES (1,
        UNHEX(REPLACE('86ed98c6-b001-48bb-b31e-da638b979c72', '-', '')),
        'MyOrganisation',
        'Calculates the median for the DemoSamples');

INSERT INTO `ObserverOrganisations`
(`observer_id`,
 `organisation_id`)
VALUES (1,
        1);

CREATE TABLE `WebRTCObserver`.`PeerConnectionSSRCs`
(
    `observerUUID`        BINARY(16) NOT NULL COMMENT 'The UUID of the observer the SSRC belongs to',
    `SSRC`                BIGINT NOT NULL COMMENT 'The SSRC identifier',
    `peerConnectionUUID`  BINARY(16) NOT NULL COMMENT 'The UUID of the peer connection the SSRC belongs to',
    `updated`             TIMESTAMP,
    `timeZone`            VARCHAR (64),
    PRIMARY KEY (`peerConnectionUUID`,`SSRC`),
    INDEX `PeerconnectionSSRCs_updated_index` (`updated`),
    INDEX `PeerconnectionSSRCs_peerConnection_index` (`peerConnectionUUID`),
    INDEX `PeerconnectionSSRCs_observer_ssrc_index` (`observerUUID`, `SSRC`)  
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='A table to map peer connections to SSRCs';
  
CREATE TABLE `WebRTCObserver`.`CallPeerconnections`
(
    `peerConnectionUUID` BINARY(16) NOT NULL COMMENT 'The UUID of the peer connection the SSRC belongs to',
    `callUUID`         BINARY(16) NOT NULL COMMENT 'The UUID of the call the peer connection belongs to',
    PRIMARY KEY (`peerConnectionUUID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='CallIDs';
  


CREATE TABLE `WebRTCObserver`.`ReportedPeerConnections`
(
    `peerConnectionUUID` BINARY(16) NOT NULL COMMENT 'The UUID of the peer connection the report is originated from',
    `callUUID` BINARY(16) NOT NULL COMMENT 'The UUID of the peer connection the report is originated from',
    `observerUUID` BINARY(16) NOT NULL COMMENT 'The UUID of the observer the report is originated from',
    `status`      ENUM ('JOINED','DETACHED'),
    `updated`    TIMESTAMP,
    PRIMARY KEY (`peerConnectionUUID`),
    KEY `reported_peer_connections__updated_index` (`updated`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='ReportedPeerConnections';
  
  
CREATE TABLE `WebRTCObserver`.`ReportedCalls`
(
    `callUUID` BINARY(16) NOT NULL COMMENT 'The UUID of the peer connection the report is originated from',
    `observerUUID` BINARY(16) NOT NULL COMMENT 'The UUID of the observer the report is originated from',
    `status`      ENUM ('INITIATED','FINISHED'),
    `updated`    TIMESTAMP,
    PRIMARY KEY (`callUUID`),
    KEY `reported_calls_updated_index` (`updated`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='ReportedCalls';