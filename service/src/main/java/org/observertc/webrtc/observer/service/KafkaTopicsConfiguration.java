package org.observertc.webrtc.observer.service;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("kafkaTopics")
public class KafkaTopicsConfiguration {
	public String observeRTCCIceStatsSample;
	public String observeRTCMediaStreamStatsSamples;
	public String observerSSRCPeerConnectionSamples;
}

