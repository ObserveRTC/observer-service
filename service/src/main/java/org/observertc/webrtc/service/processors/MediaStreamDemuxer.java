package org.observertc.webrtc.service.processors;//package com.observertc.gatekeeper.webrtcstat.processors.samples;

import java.util.UUID;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.observertc.webrtc.service.dto.webextrapp.RTCStatsType;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaStreamDemuxer {
	private static final int DEFAULT_OUTPUT_INDEX = 2;
	private static final int OUTBOUND_STREAM_OUTPUT_INDEX = 1;
	private static final int INBOUND_STREAM_OUTPUT_INDEX = 0;

	private static final Logger logger = LoggerFactory.getLogger(MediaStreamDemuxer.class);


	private final KStream<UUID, ObserveRTCMediaStreamStatsSample> source;
	private final KStream<UUID, ObserveRTCMediaStreamStatsSample> inboundStream;
	private final KStream<UUID, ObserveRTCMediaStreamStatsSample> defaultStream;
	private final KStream<UUID, ObserveRTCMediaStreamStatsSample> outboundStream;


	public MediaStreamDemuxer(KStream<UUID, ObserveRTCMediaStreamStatsSample> source) {
		this.source = source;
//		Predicate<UUID, ObserveRTCMediaStreamStatsSample>[] predicates = this.getPredicates();
		this.inboundStream = source.filter(new Predicate<UUID, ObserveRTCMediaStreamStatsSample>() {
			@Override
			public boolean test(UUID key, ObserveRTCMediaStreamStatsSample value) {
				if (value == null || value.rtcStats == null || value.rtcStats.getType() == null) {
					return false;
				}
				return value.rtcStats.getType().equals(RTCStatsType.INBOUND_RTP);
			}
		});

		this.outboundStream = source.filter(new Predicate<UUID, ObserveRTCMediaStreamStatsSample>() {
			@Override
			public boolean test(UUID key, ObserveRTCMediaStreamStatsSample value) {
				if (value == null || value.rtcStats == null || value.rtcStats.getType() == null) {
					return false;
				}
				return value.rtcStats.getType().equals(RTCStatsType.OUTBOUND_RTP);
			}
		});
		this.defaultStream = source.filter(new Predicate<UUID, ObserveRTCMediaStreamStatsSample>() {
			@Override
			public boolean test(UUID key, ObserveRTCMediaStreamStatsSample value) {
				return true;
			}
		});
//		this.branches = this.source.branch(predicates);
	}

	public KStream<UUID, ObserveRTCMediaStreamStatsSample> getDefaultOutputBranch() {
//		return this.branches[DEFAULT_OUTPUT_INDEX];
		return this.defaultStream;
	}


	public KStream<UUID, ObserveRTCMediaStreamStatsSample> getOutboundStreamBranch() {
//		return this.branches[OUTBOUND_STREAM_OUTPUT_INDEX];
		return this.outboundStream;
	}

	public KStream<UUID, ObserveRTCMediaStreamStatsSample> getInboundStreamBranch() {
//		return this.branches[INBOUND_STREAM_OUTPUT_INDEX];
		return this.inboundStream;
	}


//	private Predicate<UUID, ObserveRTCMediaStreamStatsSample>[] getPredicates() {
//		Predicate<UUID, ObserveRTCMediaStreamStatsSample>[] result = (Predicate<UUID, ObserveRTCMediaStreamStatsSample>[]) new Predicate<
//				?, ?>[3];
//		result[DEFAULT_OUTPUT_INDEX] = new Predicate<UUID, ObserveRTCMediaStreamStatsSample>() {
//			@Override
//			public boolean test(UUID key, ObserveRTCMediaStreamStatsSample value) {
//				return true;
//			}
//		};
//
//		result[OUTBOUND_STREAM_OUTPUT_INDEX] = new Predicate<UUID, ObserveRTCMediaStreamStatsSample>() {
//			@Override
//			public boolean test(UUID key, ObserveRTCMediaStreamStatsSample value) {
//				if (value == null || value.rtcStats == null || value.rtcStats.getType() == null) {
//					return false;
//				}
//				return value.rtcStats.getType().equals(RTCStatsType.OUTBOUND_RTP);
//			}
//		};
//
//		result[INBOUND_STREAM_OUTPUT_INDEX] = new Predicate<UUID, ObserveRTCMediaStreamStatsSample>() {
//			@Override
//			public boolean test(UUID key, ObserveRTCMediaStreamStatsSample value) {
//				if (value == null || value.rtcStats == null || value.rtcStats.getType() == null) {
//					return false;
//				}
//				return value.rtcStats.getType().equals(RTCStatsType.INBOUND_RTP);
//			}
//		};
//		return (Predicate<UUID, ObserveRTCMediaStreamStatsSample>[]) result;
//	}


}
