package org.observertc.observer.evaluators;

import org.observertc.observer.repositories.Call;
import org.observertc.observer.repositories.Client;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.samples.ServiceRoomId;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface CallsFetcher {

    final static CallsFetcherResult EMPTY_RESULT = new CallsFetcherResult(
            Collections.EMPTY_MAP,
            Collections.EMPTY_MAP,
            Collections.EMPTY_SET
    );

    record CallsFetcherResult(
            /**
             * This is the map for the calls Rooms are pointing to, and expected all
             * clients to reports to.
             */
            Map<ServiceRoomId, Call> actualCalls,

            /**
             * This is the map for clients (the key is a clientId) which calls are
             * not the activeCall pointed by the existing Room, but there calls still exists
             * for clients reporting to it noted as remedy clients
             */
            Map<String, Client> existingRemedyClients,

            /**
             * This is a set for clients, whose callId pointing to a remedy call, but the reporting clients are
             * never registered in observer. So they are new clients reporting to an old call not accept
             * new clients anymore
             */
            Set<String> unregisteredRemedyClientIds
    ) {

    }

    CallsFetcherResult fetchFor(ObservedClientSamples observedClientSamples);
}
