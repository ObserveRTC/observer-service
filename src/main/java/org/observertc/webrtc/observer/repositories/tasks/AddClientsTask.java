package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.entities.ClientEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class AddClientsTask extends ChainedTask<List<CallEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(AddClientsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private Map<UUID, ClientDTO> clientDTOs = new HashMap<>();

    @PostConstruct
    void setup() {
        new Builder<List<CallEventReport.Builder>>(this)
                .<Map<UUID, ClientDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedClientDTOs -> {
                            if (Objects.nonNull(receivedClientDTOs)) {
                                this.clientDTOs.putAll(receivedClientDTOs);
                            }
                        }
                )
                .<Map<UUID, ClientEntity>> addBreakCondition((resultHolder) -> {
                    if (this.clientDTOs.size() < 1) {
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add Client DTOs",
                // action
                () -> {
                    hazelcastMaps.getClients().putAll(this.clientDTOs);
                },
                // rollback
                (inputHolder, thrownException) -> {
                    for (UUID clientId : this.clientDTOs.keySet()) {
                        this.hazelcastMaps.getClients().remove(clientId);
                    }
                })
                .addActionStage("Bind CallIds",
                // action
                () -> {
                    this.clientDTOs.forEach((clientId, clientDTO) -> {
                        this.hazelcastMaps.getCallToClientIds().put(clientDTO.callId, clientId);
                    });
                },
                // rollback
                (inputHolder, thrownException) -> {
                    this.clientDTOs.forEach((clientId, clientDTO) -> {
                        this.hazelcastMaps.getCallToClientIds().remove(clientDTO.callId, clientId);
                    });
                })
                .addTerminalSupplier("Completed", () -> {
                    List<CallEventReport.Builder> result = this.clientDTOs.values().stream()
                            .map(this::makeReportBuilder)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return result;
                })
                .build();
    }

    public AddClientsTask withClientDTO(ClientDTO clientDTO) {
        if (Objects.isNull(clientDTO)) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        this.clientDTOs.put(clientDTO.clientId, clientDTO);
        return this;
    }

    public AddClientsTask withClientDTOs(ClientDTO... clientDTOs) {
        if (Objects.isNull(clientDTOs) || clientDTOs.length < 1) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        Arrays.stream(clientDTOs).forEach(clientDTO -> {
            this.clientDTOs.put(clientDTO.clientId, clientDTO);
        });
        return this;
    }

    public AddClientsTask withClientDTOs(Map<UUID, ClientDTO> clientDTOs) {
        if (Objects.isNull(clientDTOs) || clientDTOs.size() < 1) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        this.clientDTOs.putAll(clientDTOs);
        return this;
    }

    private CallEventReport.Builder makeReportBuilder(ClientDTO clientDTO) {
        try {
            return CallEventReport.newBuilder()
                    .setName(CallEventType.CLIENT_JOINED.name())

                    .setServiceId(clientDTO.serviceId)
                    .setRoomId(clientDTO.roomId)

                    .setMediaUnitId(clientDTO.mediaUnitId)
                    .setUserId(clientDTO.userId)
                    .setCallId(clientDTO.callId.toString())
                    .setClientId(clientDTO.clientId.toString())
                    .setTimestamp(clientDTO.joined);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for client DTO", ex);
            return null;
        }
    }
}
