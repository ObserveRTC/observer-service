package org.observertc.webrtc.service;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import java.util.List;

@ConfigurationProperties("evaluators")
public class EvaluatorsConfig {

	public ActiveStreamsConfig activeStreams;

	public SampleTransformerConfig sampleTransformer;

	public CallCleanerConfig callCleaner;

	@ConfigurationProperties("activeStreams")
	public static class ActiveStreamsConfig {
		public int updatePeriodInS;
	}

	@ConfigurationProperties("callCleaner")
	public static class CallCleanerConfig {
		public int streamMaxIdleTimeInS;
		public int streamMaxAllowedGapInS;
		public int pcRetentionTimeInDays;

	}

	@ConfigurationProperties("sampleTransformer")
	public static class SampleTransformerConfig {
		public boolean useClientTimestamp;
		public boolean reportOutboundRTP;
		public boolean reportInboundRTP;
		public boolean reportRemoteInboundRTP;
		public boolean reportTracks;
		public boolean reportMediaSource;
		public boolean reportCandidatePairs;
		public boolean reportLocalCandidates;
		public boolean reportRemoteCandidates;
		public int sentReportsCacheSize;
		public String ipFlagsConfig = null;
		public List<IPFlagConfig> ipFlags = null;

		//		@ConfigurationProperties("ipFlags")
		@EachProperty("ipFlags")
		public static class IPFlagConfig {
			public String name;
			public List<String> networks;
		}
	}


}

