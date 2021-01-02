CREATE SCHEMA IF NOT EXISTS `WebRTCObserver`;

CREATE TABLE  IF NOT EXISTS `WebRTCObserver`.`ActiveStreams`
(
    `serviceUUID`         BINARY(16),
    `SSRC`                BIGINT(8),
    `callUUID`            BINARY(16),
    PRIMARY KEY (`serviceUUID`,`SSRC`),
    INDEX `ActiveStreams_call_index` (`callUUID`)
);

CREATE TABLE IF NOT EXISTS `WebRTCObserver`.`PeerConnections`
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
);
 
