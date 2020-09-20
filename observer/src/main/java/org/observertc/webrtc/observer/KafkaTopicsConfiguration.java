package org.observertc.webrtc.observer;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("kafkaTopics")
public class KafkaTopicsConfiguration {
//	public boolean createIfNotExists = true;

	public ObservedPCSConfig observedPCS;
	public ObserveRTCReportsConfig reports;
	public ObserveRTCReportDraftsConfig reportDrafts;

	public static class TopicConfig {
		public String topicName;
		public int onCreatePartitionNums;
		public int onCreateReplicateFactor;
	}

	@ConfigurationProperties("observedPCS")
	public static class ObservedPCSConfig extends TopicConfig {

	}

	@ConfigurationProperties("observertcReports")
	public static class ObserveRTCReportsConfig extends TopicConfig {

	}

	@ConfigurationProperties("observertcReportDrafts")
	public static class ObserveRTCReportDraftsConfig extends TopicConfig {

	}

}

