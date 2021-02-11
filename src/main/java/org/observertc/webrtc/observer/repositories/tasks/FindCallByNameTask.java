package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.TaskAbstract;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.repositories.Repositories;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Prototype
public class FindCallByNameTask extends TaskAbstract<CallDTO> {

    @Inject
    Repositories repositories;

    private boolean removeUnboundNames = false;
    private UUID serviceUUID;
    private String callName;

    public FindCallByNameTask forServiceAndCall(UUID serviceUUID, String callName) {
        this.serviceUUID = serviceUUID;
        this.callName = callName;
        return this;
    }

    public FindCallByNameTask removeIfCallNameIsNotBound(boolean value) {
        this.removeUnboundNames = value;
        return this;
    }

    @Override
    protected CallDTO perform() throws Throwable {
        // tries to look for calls by name.
        if (Objects.isNull(this.callName)) {
            return null;
        }
        Collection<UUID> foundCollection = this.repositories.getCallNames(this.serviceUUID).get(this.callName);

        if (Objects.isNull(foundCollection)) {
            return null;
        }
        Set<UUID> foundCallUUIDs = foundCollection.stream().collect(Collectors.toSet());
        if (foundCallUUIDs.size() < 1) {
            return null;
        }

        // if we have found some callUUIDs then we try to retrieve one.
        UUID foundCallUUID = null;
        if (1 == foundCallUUIDs.size()) {
            foundCallUUID = foundCallUUIDs.stream().findFirst().get();
        } else {
            this.getLogger().warn("There are multiple call has found for call name {} in service {}, " +
                    "but there is no SSRC we can use to determine which call it belongs to." +
                    "In this implementation we choose the first available one.", this.callName, this.serviceUUID);
            // TODO: maybe mark those calls and exemine it in a cleaner process?
            foundCallUUID = foundCallUUIDs.stream().findFirst().get();
        }

        if (Objects.isNull(foundCallUUID)) {
            return null;
        }

        // we found one callUUID, so let's try to retrieve the callDTO for it
        CallDTO foundCallDTO = this.repositories.getCallDTOs().get(foundCallUUID);
        if (Objects.isNull(foundCallDTO)) {
            if (this.removeUnboundNames) {
                this.getLogger().warn("call uuid {} for call name {} does not exists in callDTOs map. Removing the calName now from callNamesMap",
                        foundCallUUID, this.callName);
                this.repositories.getCallNames(this.serviceUUID).remove(this.callName);
            }
            return null;
        }
        return foundCallDTO;
    }
}
