package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.PeerConnectionDTO;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Prototype
public class AddPeerConnectionsTask extends ChainedTask<List<CallEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(AddPeerConnectionsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    private Map<UUID, PeerConnectionDTO> peerConnectionDTOs = new HashMap<>();


    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<List<CallEventReport.Builder>>(this)
                .<Map<UUID, PeerConnectionDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedPeerConnectionEntities -> {
                            if (Objects.nonNull(receivedPeerConnectionEntities)) {
                                this.peerConnectionDTOs.putAll(receivedPeerConnectionEntities);
                            }
                        }
                )
                .<Map<UUID, PeerConnectionDTO>> addBreakCondition((resultHolder) -> {
                    if (this.peerConnectionDTOs.size() < 1) {
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add PeerConnection DTOs",
                        // action
                        () -> {
                            hazelcastMaps.getPeerConnections().putAll(this.peerConnectionDTOs);
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            for (UUID peerConnectionId : this.peerConnectionDTOs.keySet()) {
                                hazelcastMaps.getPeerConnections().remove(peerConnectionId);
                            }
                        })
                .addActionStage("Bind PeerConnection Ids to Client Ids",
                        // action
                        () -> {
                            Map<UUID, List<PeerConnectionDTO>> clientsToPeerConnectionDTOs =
                                    this.peerConnectionDTOs.values().stream()
                                            .collect(groupingBy(peerConnectionDTO -> peerConnectionDTO.clientId));

                            clientsToPeerConnectionDTOs.forEach((clientId, peerConnectionDTOs) -> {
                                peerConnectionDTOs.forEach(peerConnectionDTO -> {
                                    this.hazelcastMaps.getClientToPeerConnectionIds().put(clientId, peerConnectionDTO.peerConnectionId);
                                });
                            });
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            this.peerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
                                this.hazelcastMaps.getClientToPeerConnectionIds().remove(peerConnectionDTO.clientId, peerConnectionId);
                            });
                        })
                .addTerminalSupplier("Completed", () -> {
                    List<CallEventReport.Builder> result = this.peerConnectionDTOs.values().stream()
                            .map(this::makeReportBuilder)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return result;
                })
                .build();
    }


    public AddPeerConnectionsTask withPeerConnectionDTOs(Map<UUID, PeerConnectionDTO> peerConnectionDTOs) {
        if (Objects.isNull(peerConnectionDTOs) || peerConnectionDTOs.size() < 1) {
            this.getLogger().info("peerConnectionDTOs was not given to be removed");
            return this;
        }
        peerConnectionDTOs.values().stream().filter(Utils::nonNull).forEach(peerConnectionDTO -> {
            this.peerConnectionDTOs.put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
        });
        return this;
    }

    private CallEventReport.Builder makeReportBuilder(PeerConnectionDTO peerConnectionDTO) {
        try {
            return CallEventReport.newBuilder()
                    .setCallId(peerConnectionDTO.callId.toString())
                    .setServiceId(peerConnectionDTO.serviceId)
                    .setRoomId(peerConnectionDTO.roomId)

                    .setMediaUnitId(peerConnectionDTO.mediaUnitId)
                    .setUserId(peerConnectionDTO.userId)

                    .setName(CallEventType.PEER_CONNECTION_OPENED.name())
                    .setPeerConnectionId(peerConnectionDTO.peerConnectionId.toString())
                    .setClientId(peerConnectionDTO.clientId.toString())
                    .setTimestamp(peerConnectionDTO.created);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for client DTO", ex);
            return null;
        }
    }
}
