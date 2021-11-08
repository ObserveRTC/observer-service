package org.observertc.webrtc.observer.repositories;

import org.jeasy.random.EasyRandom;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HazelcastMapTestUtils {

    private static EasyRandom generator = new EasyRandom();

    @Inject
    HazelcastMaps hazelcastMaps;

}
