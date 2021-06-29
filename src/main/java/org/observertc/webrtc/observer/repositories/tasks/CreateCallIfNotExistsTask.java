package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.samples.ServiceRoomId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Prototype
public class CreateCallIfNotExistsTask extends ChainedTask<UUID> {

    private static final Logger logger = LoggerFactory.getLogger(CreateCallIfNotExistsTask.class);

    private static final String LOCK_NAME = "observertc-call-adder-lock";

    private final CallDTO.Builder callDTOBuilder = CallDTO.builder();
    private ServiceRoomId serviceRoomId = null;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FindCallIdsByServiceRoomIds findCallIds;

    @Inject
    FetchCallsTask fetchCallsTask;

    @Inject
    Provider<RemoveClientsTask> removeClientsTaskProvider;

    @Inject
    WeakLockProvider weakLockProvider;


    @PostConstruct
    void setup() {
        new Builder<>(this)
            .withLockProvider(() -> weakLockProvider.autoLock(LOCK_NAME))
            .<UUID> addConsumerEntry("Merge All Inputs",
                () -> {
                    UUID callId = UUID.randomUUID();
                    this.callDTOBuilder
                            .withCallId(callId);
                },
                callId -> {
                    this.callDTOBuilder.withCallId(callId);
                }
            )
            // Check if call already exists
            .addBreakCondition(resultHolder -> {
                this.findCallIds.whereServiceRoomIds(Set.of(this.serviceRoomId));
                if (!this.findCallIds.execute().succeeded()) {
                    throw new RuntimeException("Cannot execute FindCall task, there is no way to check if the call exists already");
                }
                Map<ServiceRoomId, UUID> foundCallIds = this.findCallIds.getResult();
                if (0 < foundCallIds.size()) {
                    UUID foundCallId = foundCallIds.get(this.serviceRoomId);
                    if (Objects.isNull(foundCallId)) {
                        logger.warn("There was a call found for serviceRoomId ({}), but cannot be retrieved", serviceRoomId.toString());
                        return false;
                    }
                    resultHolder.set(foundCallId);
                    return true;
                }
                return false;
            })
            .<CallDTO>addSupplierStage("Add CallDTO to hazelcast",
                () -> {
                    var callDTO = this.callDTOBuilder
                            .withServiceId(this.serviceRoomId.serviceId)
                            .withRoomId(this.serviceRoomId.roomId)
                            .build();
                    this.hazelcastMaps.getCalls().put(callDTO.callId, callDTO);
                    return callDTO;
                }
            )
            .<CallDTO, CallDTO>addFunctionalStage("Register Call In Room",
                    callDTO -> {
                        var serviceRoomKey = this.serviceRoomId.getKey();
                        this.hazelcastMaps.getServiceRoomToCallIds().put(serviceRoomKey, callDTO.callId);
                        return callDTO;
                    }
            )
            .<CallDTO>addTerminalFunction("Give CallId", callDTO -> {
                return callDTO.callId;
            })
            .build();
    }

    public CreateCallIfNotExistsTask withServiceRoomId(ServiceRoomId value) {
        this.serviceRoomId = value;
        this.callDTOBuilder.withRoomId(this.serviceRoomId.roomId)
                .withServiceId(this.serviceRoomId.serviceId);
        return this;
    }

    public CreateCallIfNotExistsTask withStartedTimestamp(Long value) {
        this.callDTOBuilder.withStartedTimestamp(value);
        return this;
    }

    @Override
    protected void validate() {
        super.validate();
        Objects.requireNonNull(this.serviceRoomId);
    }
}
