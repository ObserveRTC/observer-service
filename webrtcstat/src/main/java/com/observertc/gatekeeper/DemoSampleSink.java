package com.observertc.gatekeeper;

import com.observertc.gatekeeper.dto.WebRTCStatDTO;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaClient()
public interface DemoSampleSink {

	@Topic("${isc.streams.demo.samples}")
	void send(WebRTCStatDTO sample);
}

