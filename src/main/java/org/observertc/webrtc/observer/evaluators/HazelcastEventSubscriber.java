package org.observertc.webrtc.observer.evaluators;

import com.hazelcast.core.EntryListener;
import org.observertc.webrtc.observer.dto.*;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Singleton
public class HazelcastEventSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(HazelcastEventSubscriber.class);

    private final Set<UUID> subscribedClientEntryListeners = new HashSet<>();
    private final Set<UUID> subscribedCallEntryListener = new HashSet();
    private final Set<UUID> subscribedPeerConnectionEntryListener = new HashSet();
    private final Set<UUID> subscribedMediaTrackEntryListener = new HashSet();
    private final Set<UUID> subscribedSfuEntryListeners = new HashSet();
    private final Set<UUID> subscribedSfuTransportEntryListeners = new HashSet();
    private final Set<UUID> subscribedSfuRtpStreamEntryListeners = new HashSet();

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

    public HazelcastEventSubscriber withSfuEntriesLocalListener(EntryListener<UUID, SfuDTO> entryListener) {
        UUID listenerId = this.hazelcastMaps.getSFUs().addLocalEntryListener(entryListener);
        this.subscribedSfuEntryListeners.add(listenerId);
        return this;
    }

    public HazelcastEventSubscriber withSfuTransportEntriesLocalListener(EntryListener<UUID, SfuTransportDTO> entryListener) {
        UUID listenerId = this.hazelcastMaps.getSFUTransports().addLocalEntryListener(entryListener);
        this.subscribedSfuTransportEntryListeners.add(listenerId);
        return this;
    }

    public HazelcastEventSubscriber withSfuRtpStreamEntriesLocalListener(EntryListener<UUID, SfuRtpStreamDTO> entryListener) {
        UUID listenerId = this.hazelcastMaps.getSFURtpStreams().addLocalEntryListener(entryListener);
        this.subscribedSfuRtpStreamEntryListeners.add(listenerId);
        return this;
    }

    @PreDestroy
    void teardown() {
        this.doUnsubscribe(this.subscribedCallEntryListener, this.hazelcastMaps.getCalls()::removeEntryListener);
        this.doUnsubscribe(this.subscribedClientEntryListeners, this.hazelcastMaps.getClients()::removeEntryListener);
        this.doUnsubscribe(this.subscribedPeerConnectionEntryListener, this.hazelcastMaps.getPeerConnections()::removeEntryListener);
        this.doUnsubscribe(this.subscribedMediaTrackEntryListener, this.hazelcastMaps.getMediaTracks()::removeEntryListener);
        this.doUnsubscribe(this.subscribedSfuEntryListeners, this.hazelcastMaps.getSFUs()::removeEntryListener);
        this.doUnsubscribe(this.subscribedSfuTransportEntryListeners, this.hazelcastMaps.getSFUTransports()::removeEntryListener);
        this.doUnsubscribe(this.subscribedSfuRtpStreamEntryListeners, this.hazelcastMaps.getSFURtpStreams()::removeEntryListener);
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
