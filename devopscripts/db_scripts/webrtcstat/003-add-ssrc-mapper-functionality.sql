USE `ObserveRTC`;
 
CREATE TABLE `SSRCMap`
(
    `SSRC`            BIGINT NOT NULL COMMENT 'The SSRC identifier',
    `peerConnection`  BINARY(16) NOT NULL COMMENT 'The UUID of the peer connection the SSRC belongs to',
    `observer`        BINARY(16) NOT NULL COMMENT 'The UUID of the observer the SSRC belongs to',
    `updated`         TIMESTAMP,
    PRIMARY KEY (`SSRC`,`observer`,`peerConnection`),
    INDEX `SSRCMap_updated_index` (`updated`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='SSRCMap';
  
CREATE TABLE `CallMap`
(
    `peerConnection` BINARY(16) NOT NULL COMMENT 'The UUID of the peer connection the SSRC belongs to',
    `callID`         BINARY(16) NOT NULL COMMENT 'The UUID of the call the peer connection belongs to',
    PRIMARY KEY (`peerConnection`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='CallIDs';
  