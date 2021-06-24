package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.CallEventTypeVisitor;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.TimeLimitedMap;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.samples.MediaTrackId;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Prototype
public class CreateCallEventReports extends ChainedTask<List<CallEventReport>> {

    private static final Logger logger = LoggerFactory.getLogger(CreateCallEventReports.class);

    // Stage 1
    private String message = null;
    private CallEventType callEventType;
    private Set<UUID> callIds = new HashSet<>();
    private Set<UUID> clientIds = new HashSet<>();
    private Set<UUID> peerConnectionIds = new HashSet<>();
    private Set<String> mediaTrackKeys = new HashSet<>();

    // Stage 2
    private Map<UUID, CallDTO> callDTOs = new HashMap<>();
    private Map<UUID, ClientDTO> clientDTOs = new HashMap<>();
    private Map<UUID, PeerConnectionDTO> peerConnectionDTOs = new HashMap<>();
    private Map<String, MediaTrackDTO> mediaTrackDTOs = new HashMap<>();

    // Stage 3
    private Map<UUID, Long> explicitClientTimestamps = new HashMap<>();
    private List<CallEventReport.Builder> callEventReportBuilders = new LinkedList<>();

    @Inject
    HazelcastMaps hazelcastMaps;


    @PostConstruct
    void setup() {
        new Builder<>(this)
            .addActionStage("Collect DTOs", () -> {
                CallEventTypeVisitor.createActionVisitor(
                        this::collectPeerConnectionDTOs,
                        this::collectPeerConnectionDTOs,
                        this::collectClientDTOs,
                        this::collectClientDTOs,
                        this::collectPeerConnectionDTOs,
                        this::collectPeerConnectionDTOs,
                        this::collectMediaTrackDTOs,
                        this::collectMediaTrackDTOs
                ).apply(null, this.callEventType);
            })
            .addActionStage("Prepare reports", () -> {
                CallEventTypeVisitor.createActionVisitor(
                        this::prepareBuildersByCalls,
                        this::prepareBuildersByCalls,
                        this::prepareBuildersByClients,
                        this::prepareBuildersByClients,
                        this::prepareBuildersByPeerConnections,
                        this::prepareBuildersByPeerConnections,
                        this::prepareBuildersByMediaTracks,
                        this::prepareBuildersByMediaTracks
                ).apply(null, this.callEventType);
            })
            .addActionStage("Setup Event specific fields", () -> {
                var reportSetter = CallEventTypeVisitor.createFunctionalVisitor(
                        this::setupCallStartedReport,
                        this::setupCallEndedReport,
                        this::setupClientJoinedReport,
                        this::setupClientLeftReport,
                        this::setupPeerConnectionCreatedReport,
                        this::setupPeerConnectionClosedReport,
                        this::setupMediaTrackAddedReport,
                        this::setupMediaTrackRemovedReport
                );
                Consumer<CallEventReport.Builder> setup = builder -> {
                    try {
                        reportSetter.apply(builder, this.callEventType);
                    } catch (Throwable t) {
                        logger.error("Error occurred during setup call report {}", this.callEventType, t);
                    }
                };
                this.callEventReportBuilders.stream()
                        .forEach(setup);

            })
            .addTerminalSupplier("Return with Created Call Event Reports", () -> {
                return this.callEventReportBuilders
                        .stream()
                        .map(this::buildReport)
                        .collect(Collectors.toList());
            })
            .build();
    }

    public CreateCallEventReports withCallDTO( CallDTO callDTO) {
        this.callDTOs.put(callDTO.callId, callDTO);
        return this;
    }

    public CreateCallEventReports withCallDTOs(Map<UUID, CallDTO> callDTOs) {
        this.callDTOs.putAll(callDTOs);
        return this;
    }

    public CreateCallEventReports withClientDTO(ClientDTO clientDTO) {
        this.clientDTOs.put(clientDTO.clientId, clientDTO);
        return this;
    }

    public CreateCallEventReports withClientDTO(ClientDTO clientDTO, Long timestamp) {
        this.explicitClientTimestamps.put(clientDTO.clientId, timestamp);
        this.clientDTOs.put(clientDTO.clientId, clientDTO);
        return this;
    }

    public CreateCallEventReports withClientDTOs(Map<UUID, ClientDTO> clientDTOs) {
        this.clientDTOs.putAll(clientDTOs);
        return this;
    }

    public CreateCallEventReports withPeerConnectionDTO( PeerConnectionDTO peerConnectionDTO) {
        this.peerConnectionDTOs.put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
        return this;
    }

    public CreateCallEventReports withPeerConnectionDTOs(Map<UUID, PeerConnectionDTO> peerConnectionDTOs) {
        this.peerConnectionDTOs.putAll(peerConnectionDTOs);
        return this;
    }

    public CreateCallEventReports withMediaTrackDTO(MediaTrackDTO mediaTrackDTO) {
        MediaTrackId mediaTrackId = MediaTrackId.make(mediaTrackDTO.peerConnectionId, mediaTrackDTO.ssrc);
        this.mediaTrackDTOs.put(mediaTrackId.getKey(), mediaTrackDTO);
        return this;
    }

    public CreateCallEventReports withMediaTrackDTOs(Map<String, MediaTrackDTO> mediaTrackDTOs) {
        this.mediaTrackDTOs.putAll(mediaTrackDTOs);
        return this;
    }

    public CreateCallEventReports withCallEventType(CallEventType callEventType) {
        this.callEventType = callEventType;
        return this;
    }

    public CreateCallEventReports withCallEventMessage(String message) {
        this.message = message;
        return this;
    }


    @Override
    protected void validate() {
        super.validate();
        Objects.requireNonNull(this.callEventType);
    }

    private void collectMediaTrackDTOs() {
        Set<String> missingMediaTrackKeys = this.mediaTrackKeys
                .stream()
                .filter(mediaTrackKey -> !this.mediaTrackDTOs.containsKey(mediaTrackKey))
                .collect(Collectors.toSet());

        if (missingMediaTrackKeys.size() < 1) {
            this.collectPeerConnectionDTOs();
            return;
        }
        Map<String, MediaTrackDTO> missingMediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(missingMediaTrackKeys);
        this.mediaTrackDTOs.putAll(missingMediaTrackDTOs);
        this.mediaTrackDTOs.values()
                .stream()
                .forEach(mediaTrackDTO -> this.peerConnectionIds.add(mediaTrackDTO.peerConnectionId));
        this.collectPeerConnectionDTOs();
    }

    private void collectPeerConnectionDTOs() {
        Set<UUID> missingPeerConnectionIds = this.peerConnectionIds
                .stream()
                .filter(callId -> !this.peerConnectionDTOs.containsKey(callId))
                .collect(Collectors.toSet());

        if (missingPeerConnectionIds.size() < 1) {
            this.collectClientDTOs();
            return;
        }
        Map<UUID, PeerConnectionDTO> missingPeerConnectionDTOs = this.hazelcastMaps.getPeerConnections().getAll(missingPeerConnectionIds);
        this.peerConnectionDTOs.putAll(missingPeerConnectionDTOs);
        this.peerConnectionDTOs.values()
                .stream()
                .forEach(peerConnectionDTO -> this.clientIds.add(peerConnectionDTO.clientId));
        this.collectClientDTOs();
    }

    private void collectClientDTOs() {
        Set<UUID> missingClientIds = this.clientIds
                .stream()
                .filter(callId -> !this.clientDTOs.containsKey(callId))
                .collect(Collectors.toSet());

        if (missingClientIds.size() < 1) {
            this.collectCallDTOs();
            return;
        }
        Map<UUID, ClientDTO> missingClientDTOs = this.hazelcastMaps.getClients().getAll(missingClientIds);
        this.clientDTOs.putAll(missingClientDTOs);
        this.clientDTOs.values()
                .stream()
                .forEach(clientDTO -> this.callIds.add(clientDTO.callId));
        this.collectCallDTOs();
    }

    private void collectCallDTOs() {
        Set<UUID> missingCallIds = this.callIds
                .stream()
                .filter(callId -> !this.callDTOs.containsKey(callId))
                .collect(Collectors.toSet());

        if (missingCallIds.size() < 1) {
            return;
        }
        Map<UUID, CallDTO> missingCallDTOs = this.hazelcastMaps.getCalls().getAll(missingCallIds);
        this.callDTOs.putAll(missingCallDTOs);
    }

    private void prepareBuildersByMediaTracks() {
        this.mediaTrackDTOs.forEach((mediaTrackKey, mediaTrackDTO) -> {

        });
        prepareBuildersByPeerConnections();
    }

    private void prepareBuildersByPeerConnections() {
        Map<UUID, List<CallEventReport.Builder>> buildersByPeerConnectionIds =
                this.callEventReportBuilders.stream()
                        .collect(groupingBy(callEventReport -> UUID.fromString(callEventReport.getPeerConnectionId())));

        this.peerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {

        });
        prepareBuildersByClients();
    }

    private void prepareBuildersByClients() {
        Map<UUID, List<CallEventReport.Builder>> buildersByClientIds =
                this.callEventReportBuilders.stream()
                        .collect(groupingBy(callEventReport -> UUID.fromString(callEventReport.getClientId())));

        this.clientDTOs.forEach((clientId, clientDTO) -> {

        });
        prepareBuildersByCalls();
    }

    private void prepareBuildersByCalls() {
        Map<UUID, List<CallEventReport.Builder>> buildersByCallIds =
                this.callEventReportBuilders.stream()
                        .collect(groupingBy(callEventReport -> UUID.fromString(callEventReport.getCallId())));

        this.clientDTOs.forEach((clientId, clientDTO) -> {

        });
    }

    private CallEventReport buildReport(CallEventReport.Builder builder) {
        return builder
                .setName(this.callEventType.name())
                .setMessage(this.message)
                .build();

    }

    private CallEventReport.Builder setupCallStartedReport(CallEventReport.Builder builder) {
        UUID callId = UUID.fromString(builder.getCallId());
        CallDTO callDTO = this.callDTOs.get(callId);
        if (Objects.isNull(callDTO)) {
            logger.warn("Cannot setup Call Event Report {}, because callDTO is missing", this.callEventType);
            return builder;
        }
        return builder
                .setTimestamp(callDTO.started);
    }

    private CallEventReport.Builder setupCallEndedReport(CallEventReport.Builder builder) {
        UUID clientId = UUID.fromString(builder.getClientId());
        Long timestamp = this.explicitClientTimestamps.getOrDefault(clientId, Instant.now().toEpochMilli());
        return builder
                .setTimestamp(timestamp);
    }

    private CallEventReport.Builder setupClientJoinedReport(CallEventReport.Builder builder) {
        UUID clientId = UUID.fromString(builder.getClientId());
        ClientDTO clientDTO = this.clientDTOs.get(clientId);
        if (Objects.isNull(clientDTO)) {
            logger.warn("Cannot setup Call Event Report {}, because clientDTO is missing", this.callEventType);
            return builder;
        }
        return builder
                .setTimestamp(clientDTO.joined);
    }

    private CallEventReport.Builder setupClientLeftReport(CallEventReport.Builder builder) {
        Long now = Instant.now().toEpochMilli();
        return builder
                .setTimestamp(now);
    }

    private CallEventReport.Builder setupPeerConnectionCreatedReport(CallEventReport.Builder builder) {
        UUID peerConnectionId = UUID.fromString(builder.getPeerConnectionId());
        PeerConnectionDTO peerConnectionDTO = this.peerConnectionDTOs.get(peerConnectionId);
        if (Objects.isNull(peerConnectionDTO)) {
            logger.warn("Cannot setup Call Event Report {}, because peerConnectionDTO is missing", this.callEventType);
            return builder;
        }
        return builder
                .setTimestamp(peerConnectionDTO.created);
    }

    private CallEventReport.Builder setupPeerConnectionClosedReport(CallEventReport.Builder builder) {
        Long now = Instant.now().toEpochMilli();
        return builder
                .setTimestamp(now);
    }

    private CallEventReport.Builder setupMediaTrackAddedReport(CallEventReport.Builder builder) {
        UUID peerConnectionId = UUID.fromString(builder.getPeerConnectionId());
        PeerConnectionDTO peerConnectionDTO = this.peerConnectionDTOs.get(peerConnectionId);
        if (Objects.isNull(peerConnectionDTO)) {
            logger.warn("Cannot setup Call Event Report {}, because peerConnectionDTO is missing", this.callEventType);
            return builder;
        }
        return builder
                .setTimestamp(peerConnectionDTO.created);
    }

    private CallEventReport.Builder setupMediaTrackRemovedReport(CallEventReport.Builder builder) {
        Long now = Instant.now().toEpochMilli();
        return builder
                .setTimestamp(now);
    }


}
