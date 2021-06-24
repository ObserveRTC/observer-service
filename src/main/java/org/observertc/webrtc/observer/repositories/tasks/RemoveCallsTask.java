package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.ClientEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.samples.ServiceRoomId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class RemoveCallsTask extends ChainedTask<Map<UUID, CallEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveCallsTask.class);

    private static final String LOCK_NAME = "observertc-call-remover-lock";

    private Set<UUID> callIds = new HashSet<>();
    private Map<UUID, CallDTO> removedCallDTOs = new HashMap<>();
    private Map<UUID, Collection<UUID>> removedClientIds = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    RemoveClientsTask removeClientsTask;

    @Inject
    WeakLockProvider weakLockProvider;

    @PostConstruct
    void setup() {
        new ChainedTask.Builder<Map<UUID, CallEntity>>(this)
                .withLockProvider(() -> weakLockProvider.autoLock(LOCK_NAME))
                .<Set<UUID>, Set<UUID>>addSupplierEntry("Fetch CallIds",
                        () -> this.callIds,
                        receivedCallIds -> {
                            this.callIds.addAll(receivedCallIds);
                            return this.callIds;
                        }
                )
                .<Set<UUID>>addBreakCondition((callIds, resultHolder) -> {
                    if (Objects.isNull(callIds)) {
                        this.getLogger().warn("No Entities have been passed");
                        resultHolder.set(Collections.EMPTY_MAP);
                        return true;
                    }
                    if (callIds.size() < 1) {
                        resultHolder.set(Collections.EMPTY_MAP);
                        return true;
                    }
                    return false;
                })
                .<Set<UUID>, Map<UUID, CallDTO>> addFunctionalStage("Remove Call DTOs",
                        // action
                        callIds -> {
                            callIds.forEach(callId -> {
                                CallDTO callDTO = this.hazelcastMaps.getCalls().remove(callId);
                                this.removedCallDTOs.put(callId, callDTO);
                            });
                            return this.removedCallDTOs;
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(callEntitiesHolder) || Objects.isNull(callEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback.");
                                return;
                            }
                            this.hazelcastMaps.getCalls().putAll(this.removedCallDTOs);
                        })
                .<Map<UUID, CallDTO>, Map<UUID, CallEntity.Builder>> addFunctionalStage("Remove Room relation",
                        removedCallDTOs -> {
                            Map<UUID, CallEntity.Builder> callEntityBuilders = new HashMap<>();
                            removedCallDTOs.forEach((callId, callDTO) -> {
                                var serviceRoomId = ServiceRoomId.make(callDTO.serviceId, callDTO.roomId);
                                var serviceRoomKey = ServiceRoomId.createKey(serviceRoomId);
                                this.hazelcastMaps.getServiceRoomToCallIds().remove(serviceRoomKey);
                                var callEntityBuilder = CallEntity.builder().withCallDTO(callDTO);
                                callEntityBuilders.put(callId, callEntityBuilder);
                            });

                            return callEntityBuilders;
                        },
                        // rollback
                        (callDTOsHolder, thrownException) -> {
                            if (Objects.isNull(callDTOsHolder) || Objects.isNull(callDTOsHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback. callEntities are null");
                                return;
                            }
                            this.removedCallDTOs.forEach((callId, callDTO) -> {
                                var serviceRoomId = ServiceRoomId.make(callDTO.serviceId, callDTO.roomId);
                                var serviceRoomKey = ServiceRoomId.createKey(serviceRoomId);
                                this.hazelcastMaps.getServiceRoomToCallIds().put(serviceRoomKey, callId);
                            });
                        })
                .<Map<UUID, CallEntity.Builder>, Map<UUID, CallEntity.Builder>> addFunctionalStage("Remove Call Client Relations",
                        // action
                        callEntityBuilders -> {
                            callEntityBuilders.forEach((callId, cellEntityBuilder) -> {
                                Collection<UUID> clientIds = this.hazelcastMaps.getCallToClientIds().remove(callId);
                                this.removedClientIds.put(callId, clientIds);
                            });
                            return callEntityBuilders;
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(callEntitiesHolder) || Objects.isNull(callEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback. callEntities are null");
                                return;
                            }
                            this.removedClientIds.forEach((callId, clientIds) -> {
                                for (UUID clientId : clientIds) {
                                    this.hazelcastMaps.getCallToClientIds().put(callId, clientId);
                                }
                            });
                        })
                .<Map<UUID, CallEntity.Builder>, Map<UUID, CallEntity.Builder>> addFunctionalStage("Remove Call Client Relations",
                        // action
                        callEntityBuilders -> {
                            Set<UUID> clientIds = this.removedClientIds
                                    .values()
                                    .stream()
                                    .flatMap(c -> c.stream())
                                    .collect(Collectors.toSet());
                            this.removeClientsTask.whereClientIds(clientIds);
                            if (!this.removeClientsTask.execute().succeeded()) {
                                throw new RuntimeException("Cannot remove call due to error occrred removing related clients");
                            }
                            Map<UUID, ClientEntity> clientEntities = this.removeClientsTask.getResult();
                            clientEntities.forEach((clientId, clientEntity) -> {
                                var callId = clientEntity.getCallId();
                                var callEntityBuilder = callEntityBuilders.get(callId);
                                if (Objects.isNull(callEntityBuilder)) {
                                    logger.warn("Cannot retrieve callEntityBuilder");
                                    return;
                                }
                                callEntityBuilder.withClientEntity(clientEntity);
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
                .addTerminalPassingStage("Completed")
                .build();
    }

    public RemoveCallsTask whereCallIds(UUID... callIds) {
        if (Objects.isNull(callIds) || callIds.length < 1) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        this.callIds.addAll(Arrays.asList(callIds));
        return this;
    }

    public RemoveCallsTask whereCallIds(Set<UUID> callIds) {
        if (Objects.isNull(callIds) || callIds.size() < 1) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        this.callIds.addAll(callIds);
        return this;
    }

}
