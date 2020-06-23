package com.observertc.gatekeeper.webrtcstat;

import com.observertc.gatekeeper.webrtcstat.model.SSRCMapEntry;
import com.observertc.gatekeeper.webrtcstat.samples.ObserveRTCCIceStatsSample;
import com.observertc.gatekeeper.webrtcstat.samples.ObserveRTCMediaStreamStatsSample;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.Body;
import java.util.UUID;

@KafkaClient()
public interface PeerConnectionSampleKafkaSinks {

	@Topic("${kafkaTopics.ObserveRTCCIceStatsSample}")
	void sendObserveRTCICEStatsSamples(@KafkaKey UUID peerConnectionUUID, @Body ObserveRTCCIceStatsSample sample);

	@Topic("${kafkaTopics.ObserveRTCMediaStreamStatsSamples}")
	void sendObserveRTCMediaStreamStatsSamples(@KafkaKey UUID peerConnectionUUID, @Body ObserveRTCMediaStreamStatsSample sample);

	@Topic("${kafkaTopics.SSRCMapEntries}")
	void sendSSRCMapEntries(@KafkaKey UUID peerConnectionUUID, @Body SSRCMapEntry ssrcMapEntry);
}

