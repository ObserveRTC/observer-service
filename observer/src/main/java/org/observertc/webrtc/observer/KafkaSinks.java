package org.observertc.webrtc.observer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.inject.Singleton;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.observer.evaluators.mediastreams.ReportDraft;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.observertc.webrtc.observer.samples.WebExtrAppSample;

@Singleton
public class KafkaSinks {
	private final KafkaTopicsConfiguration config;
	private final Producer<UUID, ObservedPCS> observedPCSProducer;
	private final Producer<UUID, WebExtrAppSample> webExtrAppSampleProducer;
	private final Producer<UUID, Report> reportProducer;
	private final Producer<UUID, ReportDraft> reportDraftProducer;


	public KafkaSinks(
			KafkaTopicsConfiguration config,
			@KafkaClient("webExtrAppSampleProducer") Producer<UUID, WebExtrAppSample> webExtrAppSampleProducer,
			@KafkaClient("observedPCSProducer") Producer<UUID, ObservedPCS> observedPCSProducer,
			@KafkaClient("reportProducer") Producer<UUID, Report> reportProducer,
			@KafkaClient("reportDraftProducer") Producer<UUID, ReportDraft> reportDraftProducer
	) {
		this.webExtrAppSampleProducer = webExtrAppSampleProducer;
		this.observedPCSProducer = observedPCSProducer;
		this.reportDraftProducer = reportDraftProducer;
		this.reportProducer = reportProducer;
		this.config = config;
	}

	public Future<RecordMetadata> sendObservedPCS(UUID peerConnectionUUID, ObservedPCS sample) {
		return this.observedPCSProducer.send(new ProducerRecord<UUID, ObservedPCS>(this.config.observedPCS.topicName, peerConnectionUUID,
				sample));
	}

	public Future<RecordMetadata> sendReport(UUID observerUUID, Report report) {
		return this.reportProducer.send(new ProducerRecord<UUID, Report>(this.config.observertcReports.topicName, observerUUID,
				report));
	}

	public Future<RecordMetadata> sendReportDraft(UUID observerUUID, ReportDraft reportDaft) {
		return this.reportDraftProducer.send(new ProducerRecord<UUID, ReportDraft>(this.config.observertcReportDrafts.topicName, observerUUID,
				reportDaft));
	}

	/**
	 * In 2021 Jan it will be deleted
	 *
	 * @param peerConnectionUUID
	 * @param webExtrAppSample
	 */
	@Deprecated
	public Future<RecordMetadata> sendWebExtrAppSamples(UUID peerConnectionUUID, WebExtrAppSample webExtrAppSample) {
		return this.webExtrAppSampleProducer.send(new ProducerRecord<UUID, WebExtrAppSample>(this.config.webExtrAppSamples.topicName,
				peerConnectionUUID, webExtrAppSample));

	}
}
