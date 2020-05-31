package com.observertc.gatekeeper.webrtcstat;

import com.observertc.gatekeeper.webrtc.models.StatsPayload;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaClient()
public interface DemoSampleSink {

	@Topic("${isc.streams.demo.samples}")
	void send(StatsPayload sample);
}

