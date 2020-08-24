package org.observertc.webrtc.observer;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("kafkaTopics")
public class KafkaTopicsConfiguration {
//	public boolean createIfNotExists = true;

	public WebExtrAppSamplesConfig webExtrAppSamples;
	public ObserveRTCReportsConfig observertcReports;
	public ObserveRTCReportDraftsConfig observertcReportDrafts;


	public static class TopicConfig {
		public String topicName;
		public int onCreatePartitionNums;
		public int onCreateReplicateFactor;
	}

	@ConfigurationProperties("webExtrAppSamples")
	public static class WebExtrAppSamplesConfig extends TopicConfig {

	}

	@ConfigurationProperties("observertcReports")
	public static class ObserveRTCReportsConfig extends TopicConfig {

	}

	@ConfigurationProperties("observertcReportDrafts")
	public static class ObserveRTCReportDraftsConfig extends TopicConfig {

	}

}

