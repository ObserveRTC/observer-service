package org.observertc.webrtc.observer.evaluators;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.ReportRecord;
import org.observertc.webrtc.observer.ReportSink;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.observertc.webrtc.schemas.reports.ReportType;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

@MicronautTest
class PCObserverTest {

	static final EasyRandom generator = new EasyRandom();

	public Subject<Map<UUID, PCState>> expiredPCs = PublishSubject.create();
	public Subject<Map<UUID, PCState>> newPCs = PublishSubject.create();
	public Subject<Map<UUID, PCState>> activePCs = PublishSubject.create();

	@Inject
	PCObserver pcObserver;

	@Test
	public void shouldObserveAsNew() throws InterruptedException {
		// Given
		AtomicReference<Map<UUID, PCState>> result = new AtomicReference<>(null);
		activePCs.subscribe(result::set);
		ObservedPCS observedPCS = generator.nextObject(ObservedPCS.class);

		// When
		this.pcObserver.onNext(observedPCS);
		Thread.sleep(20000);

		// Then
		Assertions.assertNotNull(result.get());
		PCState pcState = result.get().get(observedPCS.peerConnectionUUID);
		Assertions.assertNotNull(pcState);
		Assertions.assertEquals(pcState.peerConnectionUUID, observedPCS.peerConnectionUUID);
		Assertions.assertEquals(pcState.serviceUUID, observedPCS.serviceUUID);
		Assertions.assertEquals(pcState.browserID, observedPCS.peerConnectionSample.browserId);
		Assertions.assertEquals(pcState.callName, observedPCS.peerConnectionSample.callId);
		Assertions.assertEquals(pcState.updated, observedPCS.timestamp);
		Assertions.assertEquals(pcState.userId, observedPCS.peerConnectionSample.userId);
	}

	@MockBean(ExpiredPCsEvaluatorImpl.class)
	public ExpiredPCsEvaluator createMockedExpiredPCsEvaluator() {
		PublishSubject<ReportRecord> subject = PublishSubject.create();
		return new ExpiredPCsEvaluator() {
			@Override
			public Subject<ReportRecord> getReportsSubject() {
				return subject;
			}

			@Override
			public void onSubscribe(@NonNull Disposable d) {

			}

			@Override
			public void onNext(@NonNull Map<UUID, PCState> uuidpcStateMap) {
				expiredPCs.onNext(uuidpcStateMap);
			}

			@Override
			public void onError(@NonNull Throwable e) {

			}

			@Override
			public void onComplete() {

			}
		};
	}

	@MockBean(NewPCEvaluatorImpl.class)
	public NewPCEvaluator createMockedNewPCEvaluator() {
		PublishSubject<ReportRecord> subject = PublishSubject.create();
		return new NewPCEvaluator() {

			@Override
			public Subject<ReportRecord> getReports() {
				return subject;
			}

			@Override
			public void onSubscribe(@NonNull Disposable d) {

			}

			@Override
			public void onNext(@NonNull Map<UUID, PCState> uuidpcStateMap) {
				newPCs.onNext(uuidpcStateMap);
			}

			@Override
			public void onError(@NonNull Throwable e) {

			}

			@Override
			public void onComplete() {

			}
		};
	}

	@MockBean(ActivePCsEvaluatorImpl.class)
	public ActivePCsEvaluator createMockedActivePCsEvaluator() {
		PublishSubject<Map<UUID, PCState>> subject = PublishSubject.create();
		return new ActivePCsEvaluator() {

			@Override
			public PublishSubject<Map<UUID, PCState>> getNewPeerConnectionsSubject() {
				return subject;
			}

			@Override
			public void onSubscribe(@NonNull Disposable d) {

			}

			@Override
			public void onNext(@NonNull Map<UUID, PCState> uuidpcStateMap) {
				activePCs.onNext(uuidpcStateMap);
			}

			@Override
			public void onError(@NonNull Throwable e) {

			}

			@Override
			public void onComplete() {

			}
		};
	}

	@MockBean(ReportSink.class)
	public ReportSink createReportSink() {
		return new ReportSink() {
			@Override
			public Future<RecordMetadata> sendReport(UUID reportKey, UUID serviceUUID, String serviceName, String marker, ReportType type, Long timestamp, Object payload) {
				return null;
			}

			@Override
			public void onSubscribe(@NonNull Disposable d) {

			}

			@Override
			public void onNext(@NonNull ReportRecord reportRecord) {

			}

			@Override
			public void onError(@NonNull Throwable e) {

			}

			@Override
			public void onComplete() {

			}
		};
	}
}