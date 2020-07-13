package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("mediaStreamEvaluator")
public class MediaStreamEvaluatorConfiguration {

	public CallReportsConfiguration callReports;
	public int reportPeriodInS;
	public int updatePeriodInS;

	public OutboundStreamEvaluatorConfiguration outboundStreamEvaluator;
	public InboundStreamEvaluatorConfiguration inboundStreamEvaluator;

	@ConfigurationProperties("callReports")
	public static class CallReportsConfiguration {
		public boolean enabled;
		public int peerConnectionMaxIdleTimeInS;
		public int reportPeriodInS;
		public int updatePeriodInS;
	}

	@ConfigurationProperties("inboundStreamEvaluator")
	public static class InboundStreamEvaluatorConfiguration {
		public boolean enabled;
	}

	@ConfigurationProperties("outboundStreamEvaluator")
	public static class OutboundStreamEvaluatorConfiguration {
		public int lingerPeriodInMs;
	}

	@Override
	public String toString() {
		return String.format("\n%s:\n" +
						"\treportPeriodInS: %d\n" +
						"\tupdatePeriodInS: %d\n" +
						"\tCallReportsConfiguration:\n" +
						"\t\tenabled: %b\n" +
						"\t\tpeerConnectionMaxIdleTimeInS: %d\n" +
						"\t\treportPeriodInS: %d\n" +
						"\t\tupdatePeriodInS: %d\n",
				this.getClass().getName(),
				this.reportPeriodInS,
				this.updatePeriodInS,
				this.callReports.enabled,
				this.callReports.peerConnectionMaxIdleTimeInS,
				this.callReports.reportPeriodInS,
				this.callReports.updatePeriodInS);
	}
}

