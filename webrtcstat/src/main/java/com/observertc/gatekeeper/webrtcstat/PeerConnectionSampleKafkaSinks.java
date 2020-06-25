package com.observertc.gatekeeper.webrtcstat;

import com.observertc.gatekeeper.webrtcstat.samples.ObserveRTCCIceStatsSample;
import com.observertc.gatekeeper.webrtcstat.samples.ObserveRTCMediaStreamStatsSample;
import com.observertc.gatekeeper.webrtcstat.samples.ObserverSSRCPeerConnectionSample;
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

