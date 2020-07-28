package org.observertc.webrtc.service.processors.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.common.reports.AbstractReportProcessor;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.common.reports.InitiatedCallReport;
import org.observertc.webrtc.common.reports.JoinedPeerConnectionReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.common.reports.ReportProcessor;
import org.observertc.webrtc.service.ApplicationTimeZoneId;
import org.observertc.webrtc.service.ReportsConfig;
import org.observertc.webrtc.service.jooq.enums.ReportedcallsStatus;
import org.observertc.webrtc.service.jooq.enums.ReportedpeerconnectionsStatus;
import org.observertc.webrtc.service.jooq.tables.records.ReportedcallsRecord;
import org.observertc.webrtc.service.jooq.tables.records.ReportedpeerconnectionsRecord;
import org.observertc.webrtc.service.repositories.ReportedCallsRepository;
import org.observertc.webrtc.service.repositories.ReportedPeerConnectionsRepository;
import org.observertc.webrtc.service.subscriptions.HeartBeatListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class CallReportsGuaranteer {

	private static final Logger logger = LoggerFactory.getLogger(CallReportsGuaranteer.class);
	private final ReportedCallsRepository reportedCallsRepository;
	private final ReportedPeerConnectionsRepository reportedPeerConnectionsRepository;

	@Inject
	ApplicationTimeZoneId applicationTimeZoneId;

	private Map<UUID, Map<UUID, CallReports>> observerReports = new HashMap<>();
	private ProcessorContext context;
	private final ReportProcessor reportSaver;
	private List<ReportedcallsRecord> sentCallReports = new LinkedList<>();
	private List<ReportedpeerconnectionsRecord> sentPCReports = new LinkedList<>();
	private final ReportProcessor<Void> reportAdder;

	public CallReportsGuaranteer(ReportedCallsRepository reportedCallsRepository,
								 ReportedPeerConnectionsRepository reportedPeerConnectionsRepository,
								 ReportsConfig reportsConfig,
								 HeartBeatListener heartBeatListener
	) {
		this.reportedCallsRepository = reportedCallsRepository;
		this.reportedPeerConnectionsRepository = reportedPeerConnectionsRepository;
		this.reportSaver = this.makeReportSaver();
		this.reportAdder = this.makeReportAdder();
		if (reportsConfig.callReports.callGuarantee.enabled) {
			heartBeatListener.scheduleTask(reportsConfig.callReports.callGuarantee.runPeriodInS,
					CallReportsGuaranteer.class.getName(), () -> {
						LocalDateTime now = LocalDateTime.now(applicationTimeZoneId.getZoneId());
						LocalDateTime threshold = now.minusDays(reportsConfig.callReports.callGuarantee.retentionTimeInDays);
						reportedPeerConnectionsRepository.deleteOlderThan(threshold);
						reportedCallsRepository.deleteOlderThan(threshold);

					});
		}
	}

	public void init(ProcessorContext context) {
		this.context = context;
	}

	public void add(UUID observerUUID, Report report) {
		this.reportAdder.process(report);
	}


	private void add(UUID observerUUID, UUID callUUID, Consumer<CallReports> action) {
		Map<UUID, CallReports> callsReports = this.observerReports.getOrDefault(observerUUID, new HashMap<>());
		CallReports callReports = callsReports.getOrDefault(callUUID, new CallReports(callUUID));

		action.accept(callReports);

		callsReports.put(callUUID, callReports);
		this.observerReports.put(observerUUID, callsReports);
	}

	public void checkAndSend() {
		List<ReportedpeerconnectionsRecord> reportedPeerConnectionsRecords = new LinkedList<>();
		List<ReportedcallsRecord> reportedCallsRecords = new LinkedList<>();

		Iterator<Map.Entry<UUID, Map<UUID, CallReports>>> observerIt = this.observerReports.entrySet().iterator();
		for (; observerIt.hasNext(); ) {
			Map.Entry<UUID, Map<UUID, CallReports>> observerEntry = observerIt.next();
			UUID observerUUID = observerEntry.getKey();
			Map<UUID, CallReports> callsReports = observerEntry.getValue();
			Iterator<Map.Entry<UUID, CallReports>> callsReportsIt = callsReports.entrySet().iterator();
			for (; callsReportsIt.hasNext(); ) {
				Map.Entry<UUID, CallReports> callsReportsEntry = callsReportsIt.next();
				UUID callUUID = callsReportsEntry.getKey();
				CallReports callReports = callsReportsEntry.getValue();
				this.check(callReports);
				try {
					callReports.stream().forEach(report -> {
						context.forward(observerUUID, report);
						reportSaver.process(report);
					});
				} finally {

				}
			}
		}
		this.observerReports.clear();
		this.saveSentReports();
	}

	private void saveSentReports() {
		this.reportedPeerConnectionsRepository.updateAll(this.sentPCReports);
		this.reportedCallsRepository.updateAll(this.sentCallReports);
		this.sentCallReports.clear();
		this.sentPCReports.clear();
	}

	private ReportProcessor<Void> makeReportSaver() {
		return new AbstractReportProcessor<Void>() {
			@Override
			public Void process(JoinedPeerConnectionReport report) {
				ReportedpeerconnectionsRecord reportedPeerConnectionsRecord = new ReportedpeerconnectionsRecord(
						UUIDAdapter.toBytesOrDefault(report.peerConnectionUUID, null),
						UUIDAdapter.toBytesOrDefault(report.callUUID, null),
						UUIDAdapter.toBytesOrDefault(report.observerUUID, null),
						ReportedpeerconnectionsStatus.JOINED,
						report.joined
				);
				sentPCReports.add(reportedPeerConnectionsRecord);
				return null;
			}

			@Override
			public Void process(DetachedPeerConnectionReport report) {
				ReportedpeerconnectionsRecord reportedPeerConnectionsRecord = new ReportedpeerconnectionsRecord(
						UUIDAdapter.toBytesOrDefault(report.peerConnectionUUID, null),
						UUIDAdapter.toBytesOrDefault(report.callUUID, null),
						UUIDAdapter.toBytesOrDefault(report.observerUUID, null),
						ReportedpeerconnectionsStatus.DETACHED,
						report.detached
				);
				sentPCReports.add(reportedPeerConnectionsRecord);
				return null;
			}

			@Override
			public Void process(InitiatedCallReport report) {
				ReportedcallsRecord reportedcallsRecord = new ReportedcallsRecord(
						UUIDAdapter.toBytesOrDefault(report.callUUID, null),
						UUIDAdapter.toBytesOrDefault(report.observerUUID, null),
						ReportedcallsStatus.INITIATED,
						report.initiated
				);
				sentCallReports.add(reportedcallsRecord);
				return null;
			}

			@Override
			public Void process(FinishedCallReport report) {
				ReportedcallsRecord reportedcallsRecord = new ReportedcallsRecord(
						UUIDAdapter.toBytesOrDefault(report.callUUID, null),
						UUIDAdapter.toBytesOrDefault(report.observerUUID, null),
						ReportedcallsStatus.FINISHED,
						report.finished
				);
				sentCallReports.add(reportedcallsRecord);
				return null;
			}
		};
	}

	private ReportProcessor<Void> makeReportAdder() {
		return new AbstractReportProcessor<Void>() {
			@Override
			public Void process(JoinedPeerConnectionReport report) {
				add(report.observerUUID, report.callUUID, callReports -> {
					callReports.joinedPeerConnections.put(report.peerConnectionUUID, new CheckHolder<>(report));
				});
				return null;
			}

			@Override
			public Void process(DetachedPeerConnectionReport report) {
				add(report.observerUUID, report.callUUID, callReports -> {
					callReports.detachedPeerConnections.put(report.peerConnectionUUID, new CheckHolder<>(report));
				});
				return null;
			}

			@Override
			public Void process(InitiatedCallReport report) {
				add(report.observerUUID, report.callUUID, callReports -> {
					callReports.initiatedCallReport = new CheckHolder<>(report);
				});
				return null;
			}

			@Override
			public Void process(FinishedCallReport report) {
				add(report.observerUUID, report.callUUID, callReports -> {
					callReports.finishedCallReport = new CheckHolder<>(report);
				});
				return null;
			}
		};
	}


	private boolean check(CallReports callReports) {
		this.checkCallReportValidity(callReports);
		Set<UUID> checkedPeerConnections = new HashSet<>();
		callReports.joinedPeerConnections.entrySet().forEach(entry -> {
			this.checkPCValidity(callReports, entry.getKey());
			checkedPeerConnections.add(entry.getKey());
		});

		callReports.detachedPeerConnections.entrySet().forEach(entry -> {
			if (checkedPeerConnections.contains(entry.getKey())) {
				return;
			}
			this.checkPCValidity(callReports, entry.getKey());
		});
		return false;
	}

	private void checkPCValidity(CallReports callReports, UUID peerConnectionUUID) {
		CheckHolder<JoinedPeerConnectionReport> joinedPCHolder = callReports.joinedPeerConnections.get(peerConnectionUUID);
		CheckHolder<DetachedPeerConnectionReport> detachedHolder = callReports.detachedPeerConnections.get(peerConnectionUUID);
		boolean joined = joinedPCHolder != null;
		boolean detached = detachedHolder != null;
		if (!joined && !detached) {
			// we have nothing t check in this version
			return;
		}
		byte[] peerConnectionUUIDBytes = UUIDAdapter.toBytes(peerConnectionUUID);

		boolean valid = true;
		if (joined) {
			boolean pcIsReported = this.reportedPeerConnectionsRepository.existsById(peerConnectionUUIDBytes);
			valid = pcIsReported == false;
			if (!valid) {
				logger.warn("Peer Connection with UUID {} has already been reported", peerConnectionUUID);
			}
		} else if (detached) {
			Optional<ReportedpeerconnectionsRecord> recordHolder = this.reportedPeerConnectionsRepository.findById(peerConnectionUUIDBytes);
			if (!recordHolder.isPresent()) {
				valid = true;
				logger.info("Peer Connection with UUID {} has not been reported although it has been detached, it is reported now, and " +
								"saved into the reported peer connections",
						peerConnectionUUID);
			} else {
				ReportedpeerconnectionsRecord record = recordHolder.get();
				if (!record.getStatus().equals(ReportedpeerconnectionsStatus.JOINED)) {
					valid = false;
					logger.warn("Peer Connection with UUID {} has already been reported as detached", peerConnectionUUID);
				}
			}
		}
		if (joinedPCHolder != null) {
			joinedPCHolder.checked = true;
			joinedPCHolder.valid = valid;
		}

		if (detachedHolder != null) {
			detachedHolder.checked = true;
			detachedHolder.valid = valid;
		}
	}

	private void checkCallReportValidity(CallReports callReports) {
		boolean initiated = callReports.initiatedCallReport != null;
		boolean finished = callReports.finishedCallReport != null;
		if (!initiated && !finished) {
			// we have nothing to check. In this version, because it can be much  more complicated
			return;
		}
		byte[] callUUIDBytes = UUIDAdapter.toBytes(callReports.callUUID);
		if (initiated && finished) {
			// it must not be in the repository
			boolean callIsReported = this.reportedCallsRepository.existsById(callUUIDBytes);
			boolean valid = callIsReported == false;
			callReports.initiatedCallReport.checked = true;
			callReports.finishedCallReport.checked = true;
			if (callIsReported) {
				logger.warn("Call with UUID {} is already reported", callReports.callUUID);
			}
			callReports.initiatedCallReport.valid = valid;
			callReports.finishedCallReport.valid = valid;
			return;
		}
		Optional<ReportedcallsRecord> recordHolder = this.reportedCallsRepository.findById(callUUIDBytes);
		if (initiated) {

			// it must not be in the repository
			boolean valid = true;
			callReports.initiatedCallReport.checked = true;
			if (recordHolder.isPresent()) {
				ReportedcallsRecord record = recordHolder.get();
				logger.warn("Call with UUID {} has already been reported, with a status of {}", callReports.callUUID, record.getStatus().getName());
				valid = false;
			}
			callReports.initiatedCallReport.valid = valid;
			// If we initiate it now, we cannot have it before.
			return;
		}

		if (finished) {
			boolean valid = true;
			if (!recordHolder.isPresent()) {
				logger.warn("Call with UUID {} has not been reported, although it it is observeds as finnished call",
						callReports.callUUID);
				valid = false;
			} else {
				ReportedcallsRecord record = recordHolder.get();
				if (!record.getStatus().equals(ReportedcallsStatus.INITIATED)) {
					logger.warn("Call with UUID {} has already been reported with a status of {}", callReports.callUUID,
							record.getStatus().getName());
					valid = false;
				}
			}
			callReports.finishedCallReport.checked = true;
			callReports.finishedCallReport.valid = valid;
		}
	}


	private class CheckHolder<T extends Report> {
		final T report;
		boolean valid = false;
		boolean checked = false;

		private CheckHolder(T report) {
			this.report = report;
		}

		Report getReport() {
			return this.report;
		}
	}

	private class CallReports {

		final UUID callUUID;
		CheckHolder<InitiatedCallReport> initiatedCallReport;
		Map<UUID, CheckHolder<JoinedPeerConnectionReport>> joinedPeerConnections = new HashMap<>();
		CheckHolder<FinishedCallReport> finishedCallReport;
		Map<UUID, CheckHolder<DetachedPeerConnectionReport>> detachedPeerConnections = new HashMap<>();

		public CallReports(UUID callUUID) {
			this.callUUID = callUUID;
		}

		public Stream<? extends Report> stream() {
			Stream.Builder<Report> callReportsStreamBuilder = Stream.builder();
			if (this.initiatedCallReport != null) {
				if (this.initiatedCallReport.checked && this.initiatedCallReport.valid) {
					callReportsStreamBuilder.accept(this.initiatedCallReport.report);
				}
			}
			if (this.finishedCallReport != null) {
				if (this.finishedCallReport.checked && this.finishedCallReport.valid) {
					callReportsStreamBuilder.accept(this.finishedCallReport.report);
				}
			}

			Stream<Report> pcReportsStream = null;
			if (0 < this.joinedPeerConnections.size()) {
				pcReportsStream =
						this.joinedPeerConnections.values().stream().filter(r -> r.checked && r.valid).map(CheckHolder::getReport);
			}

			if (0 < this.detachedPeerConnections.size()) {
				Stream<Report> detachedPCStream = this.detachedPeerConnections.values().stream().filter(r -> r.checked && r.valid).map(CheckHolder::getReport);
				if (pcReportsStream != null) {
					pcReportsStream = Stream.concat(pcReportsStream, detachedPCStream);
				} else {
					pcReportsStream = detachedPCStream;
				}
			}
			return Stream.concat(callReportsStreamBuilder.build(), pcReportsStream);
		}


	}


}
