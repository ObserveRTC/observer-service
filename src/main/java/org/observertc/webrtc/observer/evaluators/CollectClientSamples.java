package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.samples.ClientSample;
import org.observertc.webrtc.observer.samples.ClientSamples;
import org.observertc.webrtc.observer.samples.CollectedClientSamples;
import org.observertc.webrtc.observer.samples.ObservedClientSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Responsible to map the received client sample into an organized map of ClientSamples,
 * so the successor components can work by a batch of samples groupped by clients
 */
@Prototype
public class CollectClientSamples implements Function<List<ObservedClientSample>, CollectedClientSamples> {

    private static final Logger logger = LoggerFactory.getLogger(CollectClientSamples.class);

    @PostConstruct
    void setup() {

    }


    @Override
    public CollectedClientSamples apply(List<ObservedClientSample> observedClientSamples) throws Throwable {
        if (Objects.isNull(observedClientSamples)) {
            return null;
        }

        Map<UUID, ClientSamples.Builder> builders = this.collectBuilders(observedClientSamples);
        CollectedClientSamples.Builder result = CollectedClientSamples.builder();
        for (var builder : builders.values()) {
            ClientSamples clientSamples;
            try {
                clientSamples = builder.build();
            } catch (Throwable t) {
                logger.warn("Building Client Samples thrown an exception", t);
                continue;
            }
            result.withClientSamples(clientSamples);
        }
        return result.build();
    }

    private Map<UUID, ClientSamples.Builder> collectBuilders(List<ObservedClientSample> observedClientSamples) {
        Map<UUID, ClientSamples.Builder> result = new HashMap<>();
        for (ObservedClientSample observedClientSample : observedClientSamples) {
            UUID clientId = observedClientSample.getClientId();
            ClientSamples.Builder builder = result.get(clientId);
            if (Objects.isNull(builder)) {
                builder = ClientSamples.builderFrom(observedClientSample);
                result.put(clientId, builder);
            }
            ClientSample clientSample = observedClientSample.getClientSample();;
            builder.withClientSample(clientSample);
        }
        return result;
    }
}
