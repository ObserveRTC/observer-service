package org.observertc.webrtc.observer.subscriptions;

import com.hazelcast.nio.serialization.HazelcastSerializationException;
import io.micronaut.scheduling.annotation.Scheduled;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Singleton
public class RepositoryCleaner {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryCleaner.class);

    private volatile boolean run = true;

    @Inject
    RepositoryProvider repositoryProvider;

    @Scheduled(initialDelay = "5m", fixedDelay = "10m")
    public void start() {
//        if (this.run) {
//            return;
//        }
        try {
            Set<UUID> keys = this.repositoryProvider.getPeerConnectionsRepository().getLocalKeySet();
            Iterator<UUID> it = keys.iterator();
            while (it.hasNext()) {
                UUID key = it.next();
                try {
                    Optional<PeerConnectionEntity> pcEntityHolder =  this.repositoryProvider.getPeerConnectionsRepository().find(key);
                    pcEntityHolder.get();
                } catch (HazelcastSerializationException ex) {
                    logger.warn("Need to delete PC {}, becasue it cannot be deserialized", key);
                    try {
                        this.repositoryProvider.getPeerConnectionsRepository().delete(key);
                    } catch (Throwable t) {
                        logger.warn("Unexpected exception during deletion", t);
                    }
                }
            }
        } catch (Throwable t) {
            logger.warn("Unexpected error during repository cleaner", t);
        }
//        this.run = true;
    }
}
