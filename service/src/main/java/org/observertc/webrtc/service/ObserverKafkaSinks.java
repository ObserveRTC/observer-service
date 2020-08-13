package org.observertc.webrtc.service;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.Body;
import java.util.UUID;
import org.observertc.webrtc.service.samples.ICEStatsSample;
import org.observertc.webrtc.service.samples.MediaStreamSample;
import org.observertc.webrtc.service.samples.WebExtrAppSample;

@KafkaClient()
public interface ObserverKafkaSinks {

	@Topic("${kafkaTopics.webExtrAppSamples}")
	void sendWebExtrAppSample(@KafkaKey UUID peerConnectionUUID, @Body WebExtrAppSample sample);

}

