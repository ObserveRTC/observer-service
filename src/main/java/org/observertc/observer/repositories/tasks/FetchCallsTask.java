package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.CallDTO;
import org.observertc.observer.entities.CallEntity;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FetchCallsTask extends ChainedTask<Map<UUID, CallEntity>> {

    private Set<UUID> callIds = new HashSet<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchClientsTask fetchClientsTask;

    @Inject
    ExposedMetrics exposedMetrics;

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        Function<Set<UUID>, Map<UUID, CallDTO>> fetchCallDTOs = clientIds -> {
            if (Objects.nonNull(clientIds)) {
                this.callIds.addAll(clientIds);
            }
            if (this.callIds.size() < 1) {
                return Collections.EMPTY_MAP;
            }
            return hazelcastMaps.getCalls().getAll(this.callIds);
        };
        new Builder<Map<UUID, CallEntity>>(this)
                .<Set<UUID>, Map<UUID, CallDTO>>addSupplierEntry("Find Call DTOs by UUIDs",
                        () -> fetchCallDTOs.apply(this.callIds),
                        callIds -> fetchCallDTOs.apply(callIds)
                )
                .<Map<UUID, CallDTO>>addBreakCondition((callDTOMap, resultHolder) -> {
                    if (Objects.isNull(callDTOMap) || callDTOMap.size() < 1) {
                        resultHolder.set(Collections.EMPTY_MAP);
                        return true;
                    }
                    return false;
                })
                .<Map<UUID, CallDTO>, Map<UUID, CallEntity.Builder>> addFunctionalStage("Convert Call DTO to Call Entity builders", callDTOMap -> {
                    Map<UUID, CallEntity.Builder> callEntityBuilders = callDTOMap.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                var callDTO = entry.getValue();
                                CallEntity.Builder builder = CallEntity.builder().withCallDTO(callDTO);
                                return builder;
                            }
                    ));
                    return callEntityBuilders;
                })
                .<Map<UUID, CallEntity.Builder>, Map<UUID, CallEntity.Builder>> addFunctionalStage("Add Client Entities", callEntityBuilders -> {
                    Set<UUID> callIds = callEntityBuilders.keySet();
                    Map<UUID, UUID> clientToCallIds = new HashMap<>();
                    callIds.forEach(callId -> {
                        Collection<UUID> clientIds = this.hazelcastMaps.getCallToClientIds().get(callId);
                        clientIds.forEach(clientId -> {
                            clientToCallIds.put(clientId, callId);
                        });
                    });
                    Set<UUID> clientIds = clientToCallIds.keySet();
                    this.fetchClientsTask.whereClientIds(clientIds);

                    if (!this.fetchClientsTask.execute().succeeded()) {
                        return callEntityBuilders;
                    }
                    var clientEntities = this.fetchClientsTask.getResult();
                    clientEntities.forEach((clientId, clientEntity) -> {
                        UUID callId = clientToCallIds.get(clientId);
                        if (Objects.isNull(callId)) {
                            return;
                        }
                        CallEntity.Builder builder = callEntityBuilders.get(callId);
                        if (Objects.isNull(builder)) {
                            return;
                        }
                        builder.withClientEntity(clientEntity);
                    });
                    return callEntityBuilders;
                })
                .<Map<UUID, CallEntity.Builder>> addTerminalFunction("Creating Call Entities", callEntityBuilders -> {
                    return callEntityBuilders.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                CallEntity.Builder callEntityBuilder = entry.getValue();
                                return callEntityBuilder.build();
                            })
                    );
                })
                .build();
    }

    public FetchCallsTask whereCallId(UUID... values) {
        if (Objects.isNull(values) || values.length < 1) {
            return this;
        }
        Arrays.asList(values)
                .stream()
                .filter(Utils::nonNull).forEach(this.callIds::add);
        return this;
    }

    public FetchCallsTask whereCallIds(Set<UUID> callIds) {
        if (Objects.isNull(callIds) || callIds.size() < 1) {
            return this;
        }
        callIds.stream().filter(Utils::nonNull).forEach(this.callIds::add);
        return this;
    }


    @Override
    protected void validate() {

    }



//    private class DataCarrier {
//
//        public CallDTO call;
//        public Set<Long> SSRCs = new HashSet<>();
//        public Map<UUID, NewPeerConnectionEntity> pcEntities = new HashMap<>();
//    }
}
