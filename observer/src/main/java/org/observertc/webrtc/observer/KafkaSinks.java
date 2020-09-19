package org.observertc.webrtc.observer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.inject.Singleton;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.observertc.webrtc.observer.evaluators.reportdrafts.ReportDraft;
import org.observertc.webrtc.observer.samples.ObservedPCS;

@Singleton
public class KafkaSinks {
	private final KafkaTopicsConfiguration config;
	private final Producer<UUID, ObservedPCS> observedPCSProducer;
	private final Producer<UUID, ReportDraft> reportDraftProducer;


	public KafkaSinks(
			KafkaTopicsConfiguration config,
			@KafkaClient("observedPCSProducer") Producer<UUID, ObservedPCS> observedPCSProducer,
			@KafkaClient("reportDraftProducer") Producer<UUID, ReportDraft> reportDraftProducer
	) {
		this.observedPCSProducer = observedPCSProducer;
		this.reportDraftProducer = reportDraftProducer;
		this.config = config;
	}

	public Future<RecordMetadata> sendObservedPCS(UUID peerConnectionUUID, ObservedPCS sample) {
		return this.observedPCSProducer.send(new ProducerRecord<UUID, ObservedPCS>(this.config.observedPCS.topicName, peerConnectionUUID,
				sample));
	}

	public Future<RecordMetadata> sendReportDraft(UUID observerUUID, ReportDraft reportDaft) {
		return this.reportDraftProducer.send(new ProducerRecord<UUID, ReportDraft>(this.config.observertcReportDrafts.topicName, observerUUID,
				reportDaft));
	}

}
