package org.observertc.webrtc.service;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("kafkaTopics")
public class KafkaTopicsConfiguration {
	public boolean createIfNotExists;
	public int replicationFactorOnCreating;
	public int partitionNumberOnCreating;
	public String webExtrAppSamples;
	public String observertcReports;
}

