package org.observertc.webrtc.observer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.inject.Singleton;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.observertc.webrtc.observer.evaluators.reportdrafts.ReportDraft;

@Singleton
public class ReportDraftSink {
	private final Producer<UUID, ReportDraft> reportDraftProducer;
	private final KafkaTopicsConfiguration config;

	public ReportDraftSink(
			KafkaTopicsConfiguration config,
			@KafkaClient("reportDrafts") Producer<UUID, ReportDraft> reportDraftProducer
	) {
		this.config = config;
		this.reportDraftProducer = reportDraftProducer;
	}

	public Future<RecordMetadata> send(UUID peerConnectionUUID, ReportDraft reportDraft) {
		return this.reportDraftProducer.send(new ProducerRecord<UUID, ReportDraft>(this.config.observertcReportDrafts.topicName, peerConnectionUUID,
				reportDraft));
	}

}
