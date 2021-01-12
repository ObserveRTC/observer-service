package org.observertc.webrtc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.core.Observable;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.samples.ObservedPCS;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

@MicronautTest
class PCObserverTest {

	static final EasyRandom generator = new EasyRandom();

	@Inject
	ObserverConfig.PCObserverConfig config;

	@Inject
    PCObserver pcObserver;

	@Test
	public void shouldObserveAsNew() throws InterruptedException {
		// Given
		ObservedPCS observedPCS = generator.nextObject(ObservedPCS.class);

		// When
		Observable
				.just(observedPCS)
				.subscribe(this.pcObserver);
		Map<UUID, PCState> result = this.pcObserver.getActivePCs().blockingFirst();

		// Then
		Assertions.assertNotNull(result);
		PCState pcState = result.get(observedPCS.peerConnectionUUID);
		Assertions.assertNotNull(pcState);
		Assertions.assertEquals(pcState.peerConnectionUUID, observedPCS.peerConnectionUUID);
		Assertions.assertEquals(pcState.serviceUUID, observedPCS.serviceUUID);
		Assertions.assertEquals(pcState.browserId, observedPCS.peerConnectionSample.browserId);
		Assertions.assertEquals(pcState.callName, observedPCS.peerConnectionSample.callId);
		Assertions.assertEquals(pcState.updated, observedPCS.timestamp);
		Assertions.assertEquals(pcState.userId, observedPCS.peerConnectionSample.userId);
	}

//	@Test
//	public void shouldObserveAsExpiredAfterSomeTime() throws InterruptedException {
//		// Given
//		ObservedPCS observedPCS = generator.nextObject(ObservedPCS.class);
//		observedPCS.timestamp = Instant.now().getEpochSecond();
//		// When
//		Observable
//				.just(observedPCS)
//				.subscribe(this.pcObserver);
//		Map<UUID, PCState> result = null;
//		int timeout = config.peerConnectionMaxIdleTimeInS + config.mediaStreamUpdatesFlushInS * 2;
//		Instant started = Instant.now();
//		for (;;) {
//			result = this.pcObserver.getExpiredPCs().blockingFirst();
//			if (Objects.nonNull(result) && 0 < result.size()) {
//				break;
//			}
//			if (started.compareTo(Instant.now().minusSeconds(timeout)) < 0) {
//				break;
//			}
//		}
//
//		// Then
//		Assertions.assertNotNull(result);
//		PCState pcState = result.get(observedPCS.peerConnectionUUID);
//		Assertions.assertNotNull(pcState);
//		Assertions.assertEquals(pcState.peerConnectionUUID, observedPCS.peerConnectionUUID);
//		Assertions.assertEquals(pcState.serviceUUID, observedPCS.serviceUUID);
//		Assertions.assertEquals(pcState.browserId, observedPCS.peerConnectionSample.browserId);
//		Assertions.assertEquals(pcState.callName, observedPCS.peerConnectionSample.callId);
//		Assertions.assertEquals(pcState.updated, observedPCS.timestamp);
//		Assertions.assertEquals(pcState.userId, observedPCS.peerConnectionSample.userId);
//	}


}