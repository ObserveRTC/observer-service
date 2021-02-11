package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.TaskStage;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.repositories.Repositories;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@Prototype
public class CallEntityFetcherTask extends ChainedTask<CallEntity> {

    private UUID callUUID = null;
    private boolean fetchPeerConnections = true;
    private boolean fetchSSRCs = true;

    @Inject
    Repositories repositories;

    @PostConstruct
    void setup() {
        new Builder<>(this)
            .addStage(TaskStage.builder("Find Call By UUID")
                    .withSupplier(() -> {
                        if (Objects.isNull(callUUID)) {
                            this.getLogger().warn("Call tried to find by provided callUUID, but the callUUID is null");
                            return null;
                        }
                        return repositories.getCallDTOs().get(callUUID);
                    })
                    .withFunction(callUUIDObj -> {
                        if (Objects.isNull(callUUIDObj)) {
                            this.getLogger().warn("Call tried to find by passed callUUID, but the callUUID is null");
                            return null;
                        }
                        UUID passedCallUUID = (UUID) callUUIDObj;
                        return repositories.getCallDTOs().get(passedCallUUID);
                    })
                    .build()
            )
            .addTerminalCondition((callDTOObjHolder, resultHolder) -> {
                if (Objects.isNull(callDTOObjHolder) || Objects.isNull(callDTOObjHolder.get())) {
                    resultHolder.set(null);
                    return true;
                }
                return false;
            })
            .addFunctionalStage("Convert CallDTO to CallEntity", callDTOObj -> {
                CallDTO callDTO = (CallDTO) callDTOObj;
                CallEntity callEntity = new CallEntity();
                callEntity.call = callDTO;
                return callEntity;
            })
            .addFunctionalStage("Fetch SSRCs", callEntityObj -> {
                CallEntity callEntity = (CallEntity) callEntityObj;
                if (!fetchSSRCs) {
                    return callEntity;
                }
                Collection<Long> SSRCs = repositories.getCallToSSRCMap(callEntity.call.serviceUUID).get(callUUID);
                callEntity.SSRCs.addAll(SSRCs);
                return callEntity;
            })
            .addFunctionalStage("Fetch Peer Connections", callEntityObj -> {
                CallEntity callEntity = (CallEntity) callEntityObj;
                if (!fetchPeerConnections) {
                    return callEntity;
                }
                // TODO: fetch peer connections
                return callEntity;
            })
            .addTerminalStageConverter("Convert to CallEntity", callEntityObj -> (CallEntity) callEntityObj)
        .build();
    }

    public CallEntityFetcherTask whereCallUUID(UUID value) {
        this.callUUID = value;
        return this;
    }

    public CallEntityFetcherTask doNotFetchPeerConnections() {
        this.fetchPeerConnections = false;
        return this;
    }

    public CallEntityFetcherTask doNotFetchSSRCs() {
        this.fetchSSRCs = false;
        return this;
    }

    @Override
    protected void validate() {
        Objects.requireNonNull(this.callUUID);
    }
}
