package org.observertc.webrtc.service.processors.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.common.reports.InitiatedCallReport;
import org.observertc.webrtc.common.reports.JoinedPeerConnectionReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.ApplicationTimeZoneId;
import org.observertc.webrtc.service.ReportsConfig;
import org.observertc.webrtc.service.model.CallPeerConnectionsEntry;
import org.observertc.webrtc.service.model.PeerConnectionSSRCsEntry;
import org.observertc.webrtc.service.repositories.CallPeerConnectionsRepository;
import org.observertc.webrtc.service.repositories.PeerConnectionSSRCsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class CallsReporter implements Punctuator, BiConsumer<UUID, Quartet<UUID, LocalDateTime, String, String>> {

	private static final Logger logger = LoggerFactory.getLogger(CallsReporter.class);

	@Inject
	ApplicationTimeZoneId applicationTimeZoneId;

	private ProcessorContext context;
	private BiConsumer<UUID, Report> reportSink;
	private final PeerConnectionSSRCsRepository peerConnectionSSRCsRepository;
	private final CallPeerConnectionsRepository callPeerConnectionsRepository;
	private final CallReportsGuaranteer callReportsGuaranteer;
	private final Map<UUID, Quartet<UUID, LocalDateTime, String, String>> updatedPeerConnections;
	private final Map<UUID, List<Report>> createdReports;
	private int peerConnectionMaxIdleTimeInS = 30; // default value

	//	private final Map<SSRCMapEntry, LocalDateTime> ssrcMapEntries;
	public CallsReporter(PeerConnectionSSRCsRepository peerConnectionSSRCsRepository,
						 CallPeerConnectionsRepository callPeerConnectionsRepository,
						 CallReportsGuaranteer callReportsGuaranteer) {
		this.peerConnectionSSRCsRepository = peerConnectionSSRCsRepository;
		this.callPeerConnectionsRepository = callPeerConnectionsRepository;
		this.callReportsGuaranteer = callReportsGuaranteer;
		this.updatedPeerConnections = new HashMap<>();
		this.createdReports = new HashMap<>();

	}


	public void init(ProcessorContext context, ReportsConfig.CallReportsConfig callReportsConfig) {
		this.peerConnectionMaxIdleTimeInS = callReportsConfig.peerConnectionMaxIdleTimeInS;
		this.context = context;
		this.callReportsGuaranteer.init(context);
		if (callReportsConfig.callGuarantee.enabled) {
			this.reportSink = this.callReportsGuaranteer::add;
		} else {
			this.reportSink = context::forward;
		}

	}


	/**
	 * This is the trigger method initiate the process of cleaning the calls
	 *
	 * @param timestamp
	 */
	@Override
	public void punctuate(long timestamp) {
		try {
			this.doPunctuate(timestamp);
		} catch (Exception ex) {
			logger.error("Call Report process is failed", ex);
		}
	}

	/**
	 * The actual process we execute
	 *
	 * @param timestamp
	 */
	private void doPunctuate(long timestamp) {
		this.identifyCalls();
		this.cleanCalls();
		this.callReportsGuaranteer.checkAndSend();
	}

	@Override
	public void accept(UUID peerConnectionUUID, Quartet<UUID, LocalDateTime, String, String> tuple) {
		this.updatedPeerConnections.put(peerConnectionUUID, tuple);
	}


	/**
	 * Updates the table for SSRC to peer connections and
	 * returns with peerConnectionUUID, ObserverUUID entries for which
	 * peerConnectoin a callUUID is missing
	 *
	 * @return
	 */
	private void identifyCalls() {
		if (this.updatedPeerConnections.size() < 1) {
			return;
		}

		Set<UUID> reportedPeerConnections = new HashSet<>();
		this.peerConnectionSSRCsRepository.findCallUUIDs(updatedPeerConnections.keySet(),
				callPeerConnectionsEntry -> {
					if (callPeerConnectionsEntry.callUUID != null) {
						return;
					}
					UUID peerConnectionUUID = callPeerConnectionsEntry.peerConnectionUUID;
					if (reportedPeerConnections.contains(peerConnectionUUID)) {
						return;
					}
					Quartet<UUID, LocalDateTime, String, String> tuple = updatedPeerConnections.get(peerConnectionUUID);
					UUID observerUUID = tuple.getValue0();
					LocalDateTime firstSampled = tuple.getValue1();
					String browserId = tuple.getValue2();
					String timeZoneId = tuple.getValue3();
					reportNewPeerConnection(peerConnectionUUID, observerUUID, firstSampled, browserId, null);
					reportedPeerConnections.add(peerConnectionUUID);
				});
		this.updatedPeerConnections.clear();
	}

	private void reportNewPeerConnection(UUID peerConnectionUUID, UUID observerUUID, LocalDateTime firstSampled, String browserID,
										 ZoneId zoneId) {
		AtomicReference<UUID> callUUIDHolder = new AtomicReference<>(null);
		Set<UUID> peers = new HashSet<>();
		this.peerConnectionSSRCsRepository.findPeers(peerConnectionUUID, peerUUID -> {
			peers.add(peerUUID);
		});
		AtomicReference<LocalDateTime> firstSampleHolder = new AtomicReference<>(firstSampled);
		if (peers.size() < 1) {
			// There are no peers for this! Do we have a one participant conference?
			// Is it somebody who joined before?
			// TODO: check if it is a new or a lonely peer connection who expired earlier
		}
		peers.add(peerConnectionUUID);
		this.peerConnectionSSRCsRepository.findCallUUIDs(peers, callPeerConnectionsEntry -> {
			if (callPeerConnectionsEntry.updated != null) {
				if (callPeerConnectionsEntry.updated.compareTo(firstSampleHolder.get()) < 0) {
					firstSampleHolder.set(callPeerConnectionsEntry.updated);
				}
			}

			if (callPeerConnectionsEntry.callUUID == null) {
				return;
			}
			UUID callUUIDCandidate = callPeerConnectionsEntry.callUUID;
			if (callUUIDHolder.get() == null) {
				callUUIDHolder.set(callUUIDCandidate);
				return;
			}
			UUID selectedCallUUID = callUUIDHolder.get();
			if (!selectedCallUUID.equals(callUUIDCandidate)) {
				logger.warn("Different CallUUID is found ({}, {}) for peers belongs to the same Observer, SSRC", callUUIDCandidate,
						selectedCallUUID);
			}
		});
		UUID callUUID = callUUIDHolder.get();
		if (callUUID == null) {
			callUUID = UUID.randomUUID();
			InitiatedCallReport initiatedCallReport = InitiatedCallReport.of(observerUUID, callUUID, firstSampleHolder.get());
			this.reportSink.accept(observerUUID, initiatedCallReport);
		}
		CallPeerConnectionsEntry callPeerConnectionsEntry = CallPeerConnectionsEntry.of(peerConnectionUUID, callUUID, firstSampled);
		JoinedPeerConnectionReport joinedPeerConnectionReport = JoinedPeerConnectionReport.of(observerUUID, callUUID, peerConnectionUUID,
				browserID, firstSampled, zoneId.getId());
		this.reportSink.accept(observerUUID, joinedPeerConnectionReport);
		this.callPeerConnectionsRepository.save(callPeerConnectionsEntry);
	}

	private void cleanCalls() {
//		LocalDateTime threshold = LocalDateTime.now(ZoneOffset.UTC).minus(this.peerConnectionMaxIdleTimeInS, ChronoUnit.SECONDS);
		LocalDateTime threshold = LocalDateTime.now(applicationTimeZoneId.getZoneId()).minus(this.peerConnectionMaxIdleTimeInS,
				ChronoUnit.SECONDS);
		List<PeerConnectionSSRCsEntry> expiredPeerConnectionSSRCsEntries =
				this.peerConnectionSSRCsRepository.findExpiredPeerConnections(threshold);
		if (expiredPeerConnectionSSRCsEntries.size() < 1) {
			return;
		}
		Iterator<PeerConnectionSSRCsEntry> it = expiredPeerConnectionSSRCsEntries.iterator();
		Set<UUID> checkedPeerConnections = new HashSet<>();
		for (; it.hasNext(); ) {
			PeerConnectionSSRCsEntry entry = it.next();
			UUID peerConnectionUUID = entry.peerConnectionUUID;
			if (checkedPeerConnections.contains(peerConnectionUUID)) {
				continue;
			}
			checkedPeerConnections.add(peerConnectionUUID);
			Iterable<PeerConnectionSSRCsEntry> allStreams = this.peerConnectionSSRCsRepository.findEntries(peerConnectionUUID);
			Iterator<PeerConnectionSSRCsEntry> allStreamsIt = allStreams.iterator();
			boolean activeStream = false;
			for (; allStreamsIt.hasNext(); ) {
				PeerConnectionSSRCsEntry streamEntry = allStreamsIt.next();
				if (threshold.compareTo(streamEntry.updated) < 0) {
					activeStream = true;
					break;
				}
			}
			if (!activeStream) {
				this.reportExpiredPeerConnection(peerConnectionUUID, entry.observerUUID, entry.updated);
			}
		}
		this.peerConnectionSSRCsRepository.deleteAll(expiredPeerConnectionSSRCsEntries);
	}


	private void reportExpiredPeerConnection(UUID peerConnectionUUID, UUID observerUUID, LocalDateTime lastSampled) {
		AtomicBoolean callHasOtherPeers = new AtomicBoolean(false);
		AtomicReference<UUID> callUUIDHolder = new AtomicReference<>(null);
		this.callPeerConnectionsRepository.findPeers(peerConnectionUUID, callPeerConnectionsEntry -> {
			callUUIDHolder.set(callPeerConnectionsEntry.callUUID);
			if (!callPeerConnectionsEntry.peerConnectionUUID.equals(peerConnectionUUID)) {
				callHasOtherPeers.set(true);
			}
		});
		if (callHasOtherPeers.get() == false) {
			FinishedCallReport finishedCallReport = FinishedCallReport.of(observerUUID, callUUIDHolder.get(), lastSampled);
			this.reportSink.accept(observerUUID, finishedCallReport);
		}
		DetachedPeerConnectionReport detachedPeerConnectionReport = DetachedPeerConnectionReport.of(observerUUID, callUUIDHolder.get(),
				peerConnectionUUID, lastSampled);
		this.reportSink.accept(observerUUID, detachedPeerConnectionReport);
		CallPeerConnectionsEntry deleteCandidate = CallPeerConnectionsEntry.of(peerConnectionUUID, callUUIDHolder.get(), null);
		this.callPeerConnectionsRepository.delete(deleteCandidate);
	}

	private void addReport(UUID callUUID, Report report) {
		List<Report> reports = this.createdReports.getOrDefault(callUUID, new LinkedList<>());
		reports.add(report);
		this.createdReports.put(callUUID, reports);
	}

	Function<JoinedPeerConnectionReport, byte[]> getSignatureForJoinedPeerConnectionReport =
			report -> UUIDAdapter.toBytes(report.peerConnectionUUID);
}
