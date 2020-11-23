/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
  DEFAULT CHARSET = utf8mb4 COMMENT ='org.observertc.webrtc.observer.A table to track the active streams';
 

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
  DEFAULT CHARSET = utf8mb4 COMMENT ='org.observertc.webrtc.observer.A table to store information related to peer connections';
 