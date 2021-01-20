package org.observertc.webrtc.observer.subscriptions;

import io.micronaut.scheduling.annotation.Scheduled;
import org.observertc.webrtc.observer.models.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.Repository;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

@Singleton
public class RepositoryCleaner {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryCleaner.class);

    private volatile boolean run = true;
    @Inject
    RepositoryProvider repositoryProvider;

    @Scheduled(initialDelay = "5m")
    public void start() {
        if (this.run) {
            return;
        }
        logger.info("{} started", Repository.class.getSimpleName());
        try {
            this.checkEntries(
                    this.repositoryProvider.getPeerConnectionsRepository(),
                    PeerConnectionEntity.class,
                    null
            );
        } catch(Throwable t) {
            logger.warn("Unexpected error occurred", t);
        }
        logger.info("{} ended", Repository.class.getSimpleName());
        this.run = true;
    }

    private void doClean() {

    }

    private<K, V> void checkEntries(Repository<K, V> repository, Class<V> valueKlass, BiConsumer<K, V> checker) {
        Set<K> keys = repository.getLocalKeySet();
        Iterator<K> it = keys.iterator();
        int retried;
        while (it.hasNext()) {
            K key = it.next();
            Optional<V> valueHolder = Optional.empty();
            for(retried = 0; retried < 3; ++retried) {
                try {
                    valueHolder = repository.find(key);
                    if (!valueHolder.isPresent()) {
                        // this should not happen!
                        break;
                    }
                } catch (Exception ex) {
                    continue;
                }
            }

            //
            if (2 < retried) {
                logger.info("In repository {}, retrieval of the entry for the key {} throws an exception {} times. It will be tried to be removed",
                        repository.getClass().getSimpleName(),
                        key,
                        retried
                );
                for (retried = 0; retried < 3; ++retried) {
                    try {
                        repository.delete(key);
                        break;
                    } catch (Throwable t) {
                        logger.warn("Unexpected error occurred during removal process", t);
                        continue;
                    }
                }
                if (2 < retried) {
                    logger.warn("Cannot delete entry for key {}", key);
                }
                continue;
            }
            if (!valueHolder.isPresent()) {
                logger.warn("For repository {}, a key {} existed locally, but the find method did not find it", repository.getClass().getSimpleName(), key);
                continue;
            }

            try {
                V value = valueHolder.get();
                if (Objects.nonNull(checker)) {
                    try {
                        checker.accept(key, value);
                    } catch (Throwable t) {
                        logger.error("Unexpected error occurred during checker process", t);
                    }
                }
                continue;
            } catch (Throwable t) {
                logger.warn("Unexpected error happened while a simple get.", t);
                continue;
            }
        }
    }
}
