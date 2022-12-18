package org.observertc.observer.hamokendpoints.websocket;

import org.observertc.observer.common.JsonUtils;

import java.util.UUID;

public class RemoteIdentifiers {
    public UUID endpointId;
    public String serverUri;

    @Override
    public String toString() {
        return JsonUtils.objectToString(this);
    }
}
