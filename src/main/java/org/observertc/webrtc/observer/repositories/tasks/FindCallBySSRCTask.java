package org.observertc.webrtc.observer.repositories.tasks;

import com.hazelcast.map.IMap;
import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.TaskAbstract;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.repositories.Repositories;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;


@Prototype
public class FindCallBySSRCTask extends TaskAbstract<CallDTO> {

    @Inject
    Repositories repositories;

    private boolean removeUnboundSSRCs = false;
    private UUID serviceUUID;
    private Set<Long> SSRCs = new HashSet<>();

    public FindCallBySSRCTask forServiceAndSSRCs(UUID serviceUUID, Set<Long> SSRCs) {
        this.serviceUUID = serviceUUID;
        this.SSRCs.addAll(SSRCs);
        return this;
    }

    public FindCallBySSRCTask removeUnboundSSRCs(boolean value) {
        this.removeUnboundSSRCs = value;
        return this;
    }

    @Override
    protected CallDTO perform() throws Throwable {
        if (Objects.isNull(this.SSRCs) || this.SSRCs.size() < 1) { // no ssrc for the call
            return null;
        }
        // tries to look for calls by SSRCs
        Map<Long, UUID> ssrcToCallUUIDs = this.repositories.getSSRCToCallMap(this.serviceUUID).getAll(this.SSRCs);
        if (Objects.isNull(ssrcToCallUUIDs) || ssrcToCallUUIDs.size() < 1) {
            return null;
        }
        Set<UUID> foundCallUUIDs = ssrcToCallUUIDs.values().stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (Objects.isNull(foundCallUUIDs) || foundCallUUIDs.size() < 1) {
            return null;
        }

        // we may found some, but than we have the same problem as with the callUUIDs
        UUID callUUID = null;
        if (1 == foundCallUUIDs.size()) {
            callUUID = foundCallUUIDs.stream().findFirst().get();
        } else {
            this.getLogger().warn("There are more than one call uuid found ({}) for SSRCs ({}). That's pretty creepy." +
                            "In the current implementation we choose the first one.",
                    ObjectToString.toString(foundCallUUIDs), ObjectToString.toString(SSRCs));
            callUUID = foundCallUUIDs.stream().findFirst().get();
        }
        if (Objects.isNull(callUUID)) {
            return null;
        }
        CallDTO foundCallDTO = this.repositories.getCallDTOs().get(callUUID);
        if (Objects.isNull(foundCallDTO)) {
            if (this.removeUnboundSSRCs) {
                this.getLogger().warn("call uuid {} for SSRCs {} does not exists in callDTOs map. Removing the SSRCs now from ssrcMap",
                        callUUID, ObjectToString.toString(SSRCs));
                IMap<Long, UUID> ssrcMap = this.repositories.getSSRCToCallMap(this.serviceUUID);
                SSRCs.forEach(ssrcMap::remove);
            }
            return null;
        }
        return foundCallDTO;
    }
}
