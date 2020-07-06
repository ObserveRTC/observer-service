package org.observertc.webrtc.observer.service;

import io.micronaut.configuration.kafka.KafkaProducerFactory;
import javax.inject.Singleton;

@Singleton
public class WebRTCStatSinks {

	private final KafkaProducerFactory kafkaProducerFactory;

	public WebRTCStatSinks(KafkaProducerFactory kafkaProducerFactory) {
		this.kafkaProducerFactory = kafkaProducerFactory;
	}


}
