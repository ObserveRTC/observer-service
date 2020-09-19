package org.observertc.webrtc.observer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.inject.Singleton;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.observertc.webrtc.common.reports.avro.Report;
import org.observertc.webrtc.common.reports.avro.ReportType;

@Singleton
public class ReportSink {
	private final Producer<UUID, byte[]> reportProducer;


	public ReportSink(
			@KafkaClient("reportProducer") Producer<UUID, byte[]> reportProducer
	) {
		this.reportProducer = reportProducer;
	}

	public Future<RecordMetadata> sendReport(UUID serviceUUID,
											 String callName,
											 String serviceID,
											 ReportType type,
											 Long timestamp,
											 Object payload) {
		Report report = Report.newBuilder()
				.setCallName(callName)
				.setServiceID(serviceID)
				.setType(type)
				.setTimestamp(timestamp)
				.setPayload(payload)
				.build();
		// TODO: make this
//		return this.reportProducer.send(new ProducerRecord<UUID, ObservedPCS>(this.config.observedPCS.topicName, peerConnectionUUID,
//				sample));
		return null;
	}

}
