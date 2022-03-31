package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class AddClientsTask extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(AddClientsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    private Map<UUID, ClientDTO> clientDTOs = new HashMap<>();

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Void>(this)
                .<Map<UUID, ClientDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedClientDTOs -> {
                            if (Objects.nonNull(receivedClientDTOs)) {
                                this.clientDTOs.putAll(receivedClientDTOs);
                            }
                        }
                )
                .<Map<UUID, ClientDTO>> addBreakCondition((resultHolder) -> {
                    if (this.clientDTOs.size() < 1) {
                        resultHolder.set(null);
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
                .addTerminalPassingStage("Completed")
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
}
