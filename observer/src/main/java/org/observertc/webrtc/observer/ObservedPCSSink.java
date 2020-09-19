package org.observertc.webrtc.observer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.inject.Singleton;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.observertc.webrtc.observer.samples.ObservedPCS;

@Singleton
public class ObservedPCSSink {
	private final KafkaTopicsConfiguration config;
	private final Producer<UUID, ObservedPCS> observedPCSProducer;


	public ObservedPCSSink(
			KafkaTopicsConfiguration config,
			@KafkaClient("observedPCSProducer") Producer<UUID, ObservedPCS> observedPCSProducer
	) {
		this.observedPCSProducer = observedPCSProducer;
		this.config = config;
	}

	public Future<RecordMetadata> sendObservedPCS(UUID peerConnectionUUID, ObservedPCS sample) {
		return this.observedPCSProducer.send(new ProducerRecord<UUID, ObservedPCS>(this.config.observedPCS.topicName, peerConnectionUUID,
				sample));
	}

}
