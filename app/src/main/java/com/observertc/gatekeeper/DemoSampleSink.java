package com.observertc.gatekeeper;

import com.observertc.gatekeeper.dto.DemoWebRTCStat;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaClient()
public interface DemoSampleSink {

	@Topic("${isc.streams.demo.samples}")
	void send(DemoWebRTCStat sample);
}

