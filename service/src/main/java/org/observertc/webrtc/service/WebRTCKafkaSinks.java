package org.observertc.webrtc.service;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.Body;
import java.util.UUID;
import org.observertc.webrtc.service.samples.ICEStatsSample;
import org.observertc.webrtc.service.samples.MediaStreamSample;

@KafkaClient()
public interface WebRTCKafkaSinks {

	@Topic("${kafkaTopics.observeRTCCIceStatsSample}")
	void sendICEStatsSamples(@KafkaKey UUID peerConnectionUUID, @Body ICEStatsSample sample);

	@Topic("${kafkaTopics.observeRTCMediaStreamStatsSamples}")
	void sendObserveRTCMediaStreamStatsSamples(@KafkaKey UUID peerConnectionUUID, @Body MediaStreamSample sample);

}

