package org.observertc.webrtc.service;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("evaluators")
public class EvaluatorsConfig {

	public ActiveStreamsConfig activeStreams;

	public RTCStatsConfig rtcStats;

	public ICEStatsConfig iceStats;

	@ConfigurationProperties("activeStreams")
	public static class ActiveStreamsConfig {
		public int updatePeriodInS;
		public int maxIdleTimeInS;
		public int waitingPeriods;
		public int retentionTimeInDays;
		public int maxAllowedUpdateGapInS;
	}


	@ConfigurationProperties("rtcStats")
	public static class RTCStatsConfig {
		public boolean useClientTimestamp;
		public boolean reportOutboundRTP;
		public boolean reportInboundRTP;
		public boolean reportRemoteInboundRTP;
		public boolean reportTracks;
		public boolean reportMediaSource;
	}

	@ConfigurationProperties("iceStats")
	public static class ICEStatsConfig {
		public boolean useClientTimestamp;
	}
}

