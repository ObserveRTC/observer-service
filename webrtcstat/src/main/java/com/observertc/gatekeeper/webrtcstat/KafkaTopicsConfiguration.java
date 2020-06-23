package com.observertc.gatekeeper.webrtcstat;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("kafkaTopics")
public class KafkaTopicsConfiguration {

	public String ObserveRTCICEStats;

	public String ObserveRTCMediaStreamStatsSamples;

	public String SSRCMapEntries;

	public String callsUUIDs;
}

