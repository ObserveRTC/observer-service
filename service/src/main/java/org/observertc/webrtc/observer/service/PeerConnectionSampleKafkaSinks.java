package org.observertc.webrtc.observer.service;

import org.observertc.webrtc.observer.service.samples.ObserveRTCCIceStatsSample;
import org.observertc.webrtc.observer.service.samples.ObserveRTCMediaStreamStatsSample;
import org.observertc.webrtc.observer.service.samples.ObserverSSRCPeerConnectionSample;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.Body;
import java.util.UUID;

@KafkaClient()
public interface PeerConnectionSampleKafkaSinks {

	@Topic("${kafkaTopics.observeRTCCIceStatsSample}")
	void sendObserveRTCICEStatsSamples(@KafkaKey UUID peerConnectionUUID, @Body ObserveRTCCIceStatsSample sample);

	@Topic("${kafkaTopics.observeRTCMediaStreamStatsSamples}")
	void sendObserveRTCMediaStreamStatsSamples(@KafkaKey UUID peerConnectionUUID, @Body ObserveRTCMediaStreamStatsSample sample);

	@Topic("${kafkaTopics.observerSSRCPeerConnectionSamples}")
	void sendObserverSSRCPeerConnectionSamples(@KafkaKey UUID peerConnectionUUID, @Body ObserverSSRCPeerConnectionSample ssrcMapEntry);
}

