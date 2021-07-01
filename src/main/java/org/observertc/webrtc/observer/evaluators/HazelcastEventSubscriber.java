package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryListener;
import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Prototype
public class HazelcastEventSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(HazelcastEventSubscriber.class);

    private final Set<UUID> subscribedClientEntryListeners = new HashSet<>();
    private final Set<UUID> subscribedCallEntryListener = new HashSet();
    private final Set<UUID> subscribedPeerConnectionEntryListener = new HashSet();
    private final Set<UUID> subscribedMediaTrackEntryListener = new HashSet();

    @Inject
    HazelcastMaps hazelcastMaps;


    @PostConstruct
    void setup() {

    }

    public HazelcastEventSubscriber withCallEntriesLocalListener(EntryListener<UUID, CallDTO> entryListener) {
        UUID listenerId = this.hazelcastMaps.getCalls().addLocalEntryListener(entryListener);
        this.subscribedCallEntryListener.add(listenerId);
        return this;
    }

    public HazelcastEventSubscriber withClientEntriesLocalListener(EntryListener<UUID, ClientDTO> entryListener) {
        UUID listenerId = this.hazelcastMaps.getClients().addLocalEntryListener(entryListener);
        this.subscribedClientEntryListeners.add(listenerId);
        return this;
    }

    public HazelcastEventSubscriber withPeerConnectionEntriesLocalListener(EntryListener<UUID, PeerConnectionDTO> entryListener) {
        UUID listenerId = this.hazelcastMaps.getPeerConnections().addLocalEntryListener(entryListener);
        this.subscribedPeerConnectionEntryListener.add(listenerId);
        return this;
    }

    public HazelcastEventSubscriber withMediaTrackEntriesLocalListener(EntryListener<UUID, MediaTrackDTO> entryListener) {
        UUID listenerId = this.hazelcastMaps.getMediaTracks().addLocalEntryListener(entryListener);
        this.subscribedMediaTrackEntryListener.add(listenerId);
        return this;
    }

    @PreDestroy
    void teardown() {
        this.doUnsubscribe(this.subscribedCallEntryListener, this.hazelcastMaps.getCalls()::removeEntryListener);
        this.doUnsubscribe(this.subscribedClientEntryListeners, this.hazelcastMaps.getClients()::removeEntryListener);
        this.doUnsubscribe(this.subscribedPeerConnectionEntryListener, this.hazelcastMaps.getPeerConnections()::removeEntryListener);
        this.doUnsubscribe(this.subscribedMediaTrackEntryListener, this.hazelcastMaps.getPeerConnections()::removeEntryListener);
    }

    private void doUnsubscribe(Set<UUID> listenerIds, Function<UUID, Boolean> unsubscribe) {
        listenerIds.forEach(listenerId -> {
            try {
                boolean success = unsubscribe.apply(listenerId);
                if (!success) {
                    logger.warn("Unsubscription was unsuccessful");
                }
            } catch (Exception ex) {
                logger.error("Error occured while unsubscribing listener", ex);
            }
        });
    }


}
