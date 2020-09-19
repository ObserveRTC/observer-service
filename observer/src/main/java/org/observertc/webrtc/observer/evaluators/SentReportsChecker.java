package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.common.reports.ICECandidatePairReport;
import org.observertc.webrtc.common.reports.ICELocalCandidateReport;
import org.observertc.webrtc.common.reports.ICERemoteCandidateReport;
import org.observertc.webrtc.common.reports.InboundRTPReport;
import org.observertc.webrtc.common.reports.InitiatedCallReport;
import org.observertc.webrtc.common.reports.JoinedPeerConnectionReport;
import org.observertc.webrtc.common.reports.MediaSourceReport;
import org.observertc.webrtc.common.reports.OutboundRTPReport;
import org.observertc.webrtc.common.reports.RemoteInboundRTPReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.common.reports.ReportProcessor;
import org.observertc.webrtc.common.reports.TrackReport;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.ObserverDateTime;
import org.observertc.webrtc.observer.evaluators.valueadapters.ReportSignatureMaker;
import org.observertc.webrtc.observer.jooq.tables.records.SentreportsRecord;
import org.observertc.webrtc.observer.repositories.SentReportsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class SentReportsChecker {

	private static Logger logger = LoggerFactory.getLogger(SentReportsChecker.class);
	private final EvaluatorsConfig config;
	private final SentReportsRepository sentReportsRepository;
	private final ReportSignatureMaker reportSignatureMaker;
	private final Set<ByteBuffer> cache;
	private final ObserverDateTime observerDateTime;
	private final ReportProcessor<UUID> pcUUIDExtractor;

	public SentReportsChecker(
			EvaluatorsConfig config,
			ObserverDateTime observerDateTime,
			SentReportsRepository sentReportsRepository,
			ReportSignatureMaker reportSignatureMaker
	) {
		this.config = config;
		this.observerDateTime = observerDateTime;
		this.sentReportsRepository = sentReportsRepository;
		this.reportSignatureMaker = reportSignatureMaker;
		this.pcUUIDExtractor = this.makePCUUIDExtractor();
		int cacheSize = this.config.sampleTransformer.sentReportsCacheSize;
		this.cache = Collections.newSetFromMap(new LinkedHashMap<ByteBuffer, Boolean>(cacheSize, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<ByteBuffer, Boolean> eldest) {
				// When to remove the eldest entry.
				return size() > cacheSize; // Size exceeded the max allowed.
			}
		});
	}


	public boolean isSent(Report report) {
		byte[] signature = this.reportSignatureMaker.process(report);
		ByteBuffer wrappedSignature = ByteBuffer.wrap(signature);
		if (this.cache.contains(wrappedSignature)) {
			return true;
		}
		this.cache.add(wrappedSignature);
		boolean exists = this.sentReportsRepository.existsBySignature(signature);
		if (!exists) {
			Long now = Instant.now().toEpochMilli();
			UUID pcUUID = pcUUIDExtractor.process(report);
			this.sentReportsRepository.update(
					new SentreportsRecord(signature, UUIDAdapter.toBytesOrDefault(pcUUID, null), now)
			);
			return false;
		}
		return true;
	}

	private ReportProcessor<UUID> makePCUUIDExtractor() {
		return new ReportProcessor<UUID>() {
			@Override
			public UUID processMediaSourceReport(MediaSourceReport mediaSourceReport) {
				return mediaSourceReport.peerConnectionUUID;
			}

			@Override
			public UUID processTrackReport(TrackReport trackReport) {
				return trackReport.peerConnectionUUID;
			}

			@Override
			public UUID processJoinedPeerConnectionReport(JoinedPeerConnectionReport report) {
				return report.peerConnectionUUID;
			}

			@Override
			public UUID processDetachedPeerConnectionReport(DetachedPeerConnectionReport report) {
				return report.peerConnectionUUID;
			}

			@Override
			public UUID processInitiatedCallReport(InitiatedCallReport report) {
				return null;
			}

			@Override
			public UUID processFinishedCallReport(FinishedCallReport report) {
				return null;
			}

			@Override
			public UUID processRemoteInboundRTPReport(RemoteInboundRTPReport report) {
				return report.peerConnectionUUID;
			}

			@Override
			public UUID processInboundRTPReport(InboundRTPReport report) {
				return report.peerConnectionUUID;
			}

			@Override
			public UUID processOutboundRTPReport(OutboundRTPReport report) {
				return report.peerConnectionUUID;
			}

			@Override
			public UUID processICECandidatePairReport(ICECandidatePairReport report) {
				return report.peerConnectionUUID;
			}

			@Override
			public UUID processICELocalCandidateReport(ICELocalCandidateReport report) {
				return report.peerConnectionUUID;
			}

			@Override
			public UUID processICERemoteCandidateReport(ICERemoteCandidateReport report) {
				return report.peerConnectionUUID;
			}
		};
	}

}
