package org.observertc.webrtc.service.evaluators.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.common.reports.InitiatedCallReport;
import org.observertc.webrtc.common.reports.JoinedPeerConnectionReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.jooq.enums.PeerconnectionsState;
import org.observertc.webrtc.service.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.service.repositories.PeerConnectionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class ReportsBuffer implements Consumer<Report> {

	private static final Logger logger = LoggerFactory.getLogger(ReportsBuffer.class);
	private ProcessorContext context;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final Deque<ReportDraft> drafts;

	public ReportsBuffer(PeerConnectionsRepository peerConnectionsRepository) {
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.drafts = new LinkedList<>();
	}

	@Override
	public void accept(Report report) {
		ReportDraft draft = new ReportDraft(report);
		this.drafts.addLast(draft);
	}

	public void init(ProcessorContext context) {
		this.context = context;
	}

	private BiConsumer<UUID, Report> output;

	public void connect(BiConsumer<UUID, Report> output) {
		this.output = output;
	}


	public void process() {
		if (this.drafts.size() < 1) {
			return;
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime threshold = now.minusSeconds(10);
		while (!this.drafts.isEmpty()) {
			ReportDraft draft = this.drafts.peekFirst();
			if (draft.checked == null) {
				draft = this.drafts.removeFirst();
				draft.checked = now;
				this.drafts.addLast(draft);
				continue;
			}
			if (threshold.compareTo(draft.checked) < 0) {
				break;
			}
			draft = this.drafts.removeFirst();
			Report report = draft.report;
			UUID observerUUID = null;
			boolean forward = false;
			boolean passed = false;
			AtomicReference<ReportDraft> newDraftHolder = new AtomicReference<>(null);
			switch (report.type) {
				case INITIATED_CALL:
					InitiatedCallReport initiatedCallReport = (InitiatedCallReport) report;
					observerUUID = initiatedCallReport.observerUUID;
					passed = this.validateInitiatedCallReport(initiatedCallReport, newDraftHolder);
					break;
				case FINISHED_CALL:
					FinishedCallReport finishedCallRepot = (FinishedCallReport) report;
					observerUUID = finishedCallRepot.observerUUID;
					passed = this.validateFinishedCallReport(finishedCallRepot, newDraftHolder);
					break;
				case JOINED_PEER_CONNECTION:
					JoinedPeerConnectionReport joinedPeerConnectionReport = (JoinedPeerConnectionReport) report;
					observerUUID = joinedPeerConnectionReport.observerUUID;
					forward = true;
					break;
				case DETACHED_PEER_CONNECTION:
					DetachedPeerConnectionReport detachedPeerConnectionReport = (DetachedPeerConnectionReport) report;
					observerUUID = detachedPeerConnectionReport.observerUUID;
					forward = true;
					break;
				default:
					forward = true;
					break;
			}
			if (forward) {
				if (observerUUID != null) {
					this.output.accept(observerUUID, report);
				} else {
					logger.warn("Reportsbuffer cannot forward report type of {}", report.type);
				}
				continue;
			}
			if (!passed) {
				ReportDraft newDraft = newDraftHolder.get();
				if (newDraft != null) {
					newDraft.checked = now;
					this.drafts.addLast(newDraft);
				}
				continue;
			}
			// passed validation!
			if (++draft.passed < 2) {
				draft.checked = now;
				this.drafts.addLast(draft);
				continue;
			}
			if (observerUUID == null || report == null) {
				logger.warn("Observer UUID or report is null, cannot be forwarded");
				continue;
			}
			this.output.accept(observerUUID, report);
//			this.context.forward(observerUUID, report);
		}
	}

	private boolean validateInitiatedCallReport(InitiatedCallReport initiatedCallReport, AtomicReference<ReportDraft> newDraftHolder) {
		Optional<PeerconnectionsRecord> firstPCHolder = this.peerConnectionsRepository
				.findByCallUUID(initiatedCallReport.callUUID)
				.filter(pc ->
						pc.getState().equals(PeerconnectionsState.joined) &&
								pc.getCreated().compareTo(initiatedCallReport.initiated) < 0
				)
				.findFirst();
		if (!firstPCHolder.isPresent()) {
			return true;
		}
		Report newReport = InitiatedCallReport.of(
				initiatedCallReport.observerUUID,
				initiatedCallReport.callUUID,
				firstPCHolder.get().getCreated()
		);
		ReportDraft newDraft = new ReportDraft(newReport);
		newDraftHolder.set(newDraft);
		return false;
	}

	private boolean validateFinishedCallReport(FinishedCallReport report, AtomicReference<ReportDraft> newDraftHolder) {
		FinishedCallReport finishedCallRepot = (FinishedCallReport) report;
		Optional<PeerconnectionsRecord> lastPCHolder = this.peerConnectionsRepository
				.findByCallUUID(finishedCallRepot.callUUID)
				.filter(pc ->
						pc.getState().equals(PeerconnectionsState.detached) &&
								0 < pc.getUpdated().compareTo(finishedCallRepot.finished)
				)
				.findFirst();
		if (!lastPCHolder.isPresent()) {
			return true;
		}
		Report newReport = FinishedCallReport.of(
				finishedCallRepot.observerUUID,
				finishedCallRepot.callUUID,
				lastPCHolder.get().getUpdated()
		);
		ReportDraft newDraft = new ReportDraft(newReport);
		newDraftHolder.set(newDraft);
		return false;
	}

	private class ReportDraft {
		public LocalDateTime checked;
		public final Report report;
		public volatile int passed = 0;

		public ReportDraft(Report report) {
			this.report = report;
			this.checked = LocalDateTime.now();
		}
	}
}
