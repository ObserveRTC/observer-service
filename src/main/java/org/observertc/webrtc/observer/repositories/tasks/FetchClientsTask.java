package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.entities.ClientEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FetchClientsTask extends ChainedTask<Map<UUID, ClientEntity>> {

    private Set<UUID> clientIds = new HashSet<>();


    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchPeerConnectionsTask fetchPeerConnectionsTask;

    @PostConstruct
    void setup() {
        Function<Set<UUID>, Map<UUID, ClientDTO>> fetchClientDTOs = clientIds -> {
            if (Objects.nonNull(clientIds)) {
                this.clientIds.addAll(clientIds);
            }
            if (this.clientIds.size() < 1) {
                return Collections.EMPTY_MAP;
            }
            return hazelcastMaps.getClients().getAll(this.clientIds);
        };
        new Builder<Map<UUID, ClientEntity>>(this)
            .<Set<UUID>, Map<UUID, ClientDTO>>addSupplierEntry("Find Client DTOs by UUIDs",
                    () -> fetchClientDTOs.apply(this.clientIds),
                    clientIds -> fetchClientDTOs.apply(clientIds)
            )
            .<Map<UUID, ClientDTO>>addBreakCondition((clientDTOMap, resultHolder) -> {
                if (Objects.isNull(clientDTOMap) || clientDTOMap.size() < 1) {
                    resultHolder.set(Collections.EMPTY_MAP);
                    return true;
                }
                return false;
            })
            .<Map<UUID, ClientDTO>, Map<UUID, ClientEntity.Builder>> addFunctionalStage("Convert Client DTO to Client Entity builders", clientDTOMap -> {
                Map<UUID, ClientEntity.Builder> clientBuilders = clientDTOMap.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            var clientDTO = entry.getValue();
                            ClientEntity.Builder builder = ClientEntity.builder().withClientDTO(clientDTO);
                            return builder;
                        }
                ));
                return clientBuilders;
            })
            .<Map<UUID, ClientEntity.Builder>, Map<UUID, ClientEntity.Builder>> addFunctionalStage("Add Peer Connection Entities", clientEntityBuilders -> {
                Set<UUID> clientIds = clientEntityBuilders.keySet();
                Map<UUID, UUID> peerConnectionToClientIds = new HashMap<>();
                clientIds.forEach(clientId -> {
                    Collection<UUID> peerConnectionIds = this.hazelcastMaps.getClientToPeerConnectionIds().get(clientId);
                    peerConnectionIds.forEach(peerConnectionId -> {
                        peerConnectionToClientIds.put(peerConnectionId, clientId);
                    });
                });
                Set<UUID> peerConnectionIds = peerConnectionToClientIds.keySet();
                this.fetchPeerConnectionsTask.wherePeerConnectionIds(peerConnectionIds);

                if (!this.fetchPeerConnectionsTask.execute().succeeded()) {
                    return clientEntityBuilders;
                }
                var pcEntities = this.fetchPeerConnectionsTask.getResult();
                pcEntities.forEach((peerConnectionId, peerConnectionEntity) -> {
                    UUID clientId = peerConnectionToClientIds.get(peerConnectionId);
                    if (Objects.isNull(clientId)) {
                        // TODO: notify a module about the inconsistency
                        return;
                    }
                    ClientEntity.Builder builder = clientEntityBuilders.get(clientId);
                    if (Objects.isNull(builder)) {
                        // TODO: notify a module about the inconsistency, although this should be impossible as we got the client Id from the builders
                        return;
                    }
                    builder.withPeerConnectionEntity(peerConnectionEntity);
                });
                return clientEntityBuilders;
            })
                .<Map<UUID, ClientEntity.Builder>> addTerminalFunction("Creating Client Entities", clientEntityBuilders -> {
                return clientEntityBuilders.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ClientEntity.Builder clientEntityBuilder = entry.getValue();
                            return clientEntityBuilder.build();
                        })
                );
            })
        .build();
    }

    public FetchClientsTask whereClientIds(UUID... values) {
        if (Objects.isNull(values) || values.length < 1) {
            return this;
        }
        this.clientIds.addAll(Arrays.asList(values));
        return this;
    }

    public FetchClientsTask whereClientIds(Set<UUID> values) {
        if (Objects.isNull(values) || values.size() < 1) {
            return this;
        }
        this.clientIds.addAll(values);
        return this;
    }

    @Override
    protected void validate() {

    }


}
