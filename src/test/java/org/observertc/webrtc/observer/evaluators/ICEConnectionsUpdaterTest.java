package org.observertc.webrtc.observer.evaluators;

import org.observertc.webrtc.observer.repositories.hazelcast.ICEConnectionsRepository;
import org.observertc.webrtc.observer.repositories.hazelcast.MediaUnitPeerConnectionsRepository;

import javax.inject.Inject;
import javax.inject.Provider;

class ICEConnectionsUpdaterTest {
    static TestInputsGenerator generator = TestInputsGenerator.builder().build();

    @Inject
    Provider<ICEConnectionObserver> subject;

    @Inject
    ICEConnectionsRepository iceConnectionsRepository;

    @Inject
    MediaUnitPeerConnectionsRepository mediaUnitPeerConnectionsRepository;

}