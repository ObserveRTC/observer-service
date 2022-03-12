package org.observertc.observer.repositories;

import org.observertc.observer.configs.ObserverConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StoredRequests {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ObserverConfig observerConfig;
}
