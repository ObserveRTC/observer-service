package org.observertc.webrtc.service.evaluators;//package com.observertc.gatekeeper.webrtcstat.processors.samples;

import java.util.UUID;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.observertc.webrtc.service.dto.webextrapp.PeerConnectionSample;
import org.observertc.webrtc.service.samples.WebExtrAppSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebExtrAppSampleDemuxer {

	private static final Logger logger = LoggerFactory.getLogger(WebExtrAppSampleDemuxer.class);

	private final KStream<UUID, WebExtrAppSample> source;
	private final KStream<UUID, WebExtrAppSample> mediaStatsStream;
	private final KStream<UUID, WebExtrAppSample> defaultStream;

	private boolean logSampleIsDroppedMessage(String field, UUID peerConnectionUUID, WebExtrAppSample sample) {
		logger.warn("Sample is dropped due to missing field: {}. Key: {}, Value: {}",
				field, peerConnectionUUID, sample);
		return false;
	}

	public WebExtrAppSampleDemuxer(KStream<UUID, WebExtrAppSample> source) {
		this.source = source.filter(new Predicate<UUID, WebExtrAppSample>() {
			@Override
			public boolean test(UUID peerConnectionUUID, WebExtrAppSample sample) {
				if (sample == null) {
					return logSampleIsDroppedMessage("Sample value", peerConnectionUUID, sample);
				}
				if (peerConnectionUUID == null) {
					return logSampleIsDroppedMessage("peerConnectionUUID key", peerConnectionUUID, sample);
				}
				if (sample.observerUUID == null) {
					return logSampleIsDroppedMessage("sample.observerUUID", peerConnectionUUID, sample);
				}
				if (sample.peerConnectionSample == null) {
					return logSampleIsDroppedMessage("sample.peerConnectionSample", peerConnectionUUID, sample);
				}
				return true;
			}
		});
		this.mediaStatsStream = this.source.filter(new Predicate<UUID, WebExtrAppSample>() {
			@Override
			public boolean test(UUID key, WebExtrAppSample value) {
				PeerConnectionSample sample = value.peerConnectionSample;
				return sample.getReceiverStats() != null || sample.getSenderStats() != null;
			}
		});

		this.defaultStream = this.source.filter(new Predicate<UUID, WebExtrAppSample>() {
			@Override
			public boolean test(UUID key, WebExtrAppSample value) {
				PeerConnectionSample sample = value.peerConnectionSample;
				if (sample.getIceStats() == null) {
					return false;
				}
				return true;
			}
		});
	}

	public KStream<UUID, WebExtrAppSample> getMediaStatsStreams() {
		return this.mediaStatsStream;
	}

	public KStream<UUID, WebExtrAppSample> getDefaultStream() {
		return this.defaultStream;
	}

}
