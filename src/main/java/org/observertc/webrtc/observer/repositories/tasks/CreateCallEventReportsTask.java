package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.CallEventTypeVisitor;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
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
class CreateCallEventReportsTask extends ChainedTask<List<CallEventReport>> {

    private static final Logger logger = LoggerFactory.getLogger(CreateCallEventReportsTask.class);

    // Stage 1
    private String message = null;
    private CallEventType callEventType;
    private Set<UUID> callIds = new HashSet<>();
    private Set<UUID> clientIds = new HashSet<>();
    private Set<UUID> peerConnectionIds = new HashSet<>();
    private Set<UUID> mediaTrackIds = new HashSet<>();

    // Stage 2
    private Map<UUID, CallDTO> callDTOs = new HashMap<>();
    private Map<UUID, ClientDTO> clientDTOs = new HashMap<>();
    private Map<UUID, PeerConnectionDTO> peerConnectionDTOs = new HashMap<>();
    private Map<UUID, MediaTrackDTO> mediaTrackDTOs = new HashMap<>();

    // Stage 3
    private Map<UUID, Long> explicitTimestamps = new HashMap<>();
    private List<CallEventReport.Builder> callEventReportBuilders = new LinkedList<>();

    @Inject
    HazelcastMaps hazelcastMaps;


    @PostConstruct
    void setup() {
        new Builder<>(this)
            .addActionStage("Collect DTOs", () -> {
                CallEventTypeVisitor.createActionVisitor(
                        this::collectCallDTOs,
                        this::collectCallDTOs,
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
                        this::setupPeerConnectionOpenedReport,
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
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
            })
            .build();
    }

    public CreateCallEventReportsTask withCallDTO(CallDTO callDTO) {
        this.addCallDTO(callDTO);
        return this;
    }

    public CreateCallEventReportsTask withCallDTOAndTimestamp(CallDTO callDTO, Long timestamp) {
        this.explicitTimestamps.put(callDTO.callId, timestamp);
        this.addCallDTO(callDTO);
        return this;
    }

    public CreateCallEventReportsTask withClientDTO(ClientDTO clientDTO) {
        this.addClientDTO(clientDTO);
        return this;
    }

    public CreateCallEventReportsTask withClientDTOAndTimestamp(ClientDTO clientDTO, Long timestamp) {
        this.explicitTimestamps.put(clientDTO.clientId, timestamp);
        this.addClientDTO(clientDTO);
        return this;
    }

    public CreateCallEventReportsTask withClientDTOs(Map<UUID, ClientDTO> clientDTOs) {
        clientDTOs.values().forEach(this::addClientDTO);
        return this;
    }

    public CreateCallEventReportsTask withPeerConnectionDTOAndTimestamp(PeerConnectionDTO peerConnectionDTO, Long timestamp) {
        this.explicitTimestamps.put(peerConnectionDTO.peerConnectionId, timestamp);
        this.addPeerConnectionDTO(peerConnectionDTO);
        return this;
    }

    public CreateCallEventReportsTask withPeerConnectionDTO(PeerConnectionDTO peerConnectionDTO) {
        this.addPeerConnectionDTO(peerConnectionDTO);
        return this;
    }

    public CreateCallEventReportsTask withMediaTrackDTOAndTimestamp(MediaTrackDTO mediaTrackDTO, Long timestamp) {
        this.explicitTimestamps.put(mediaTrackDTO.trackId, timestamp);
        this.addMediaTrackDTO(mediaTrackDTO);
        return this;
    }

    public CreateCallEventReportsTask withMediaTrackDTO(MediaTrackDTO mediaTrackDTO) {
        this.addMediaTrackDTO(mediaTrackDTO);
        return this;
    }

    public CreateCallEventReportsTask withCallEventType(CallEventType callEventType) {
        this.callEventType = callEventType;
        return this;
    }

    public CreateCallEventReportsTask withCallEventMessage(String message) {
        this.message = message;
        return this;
    }


    @Override
    protected void validate() {
        super.validate();
        Objects.requireNonNull(this.callEventType);
    }

    private void collectMediaTrackDTOs() {
        Set<UUID> missingMediaTrackIds = this.mediaTrackIds
                .stream()
                .filter(mediaTrackId -> !this.mediaTrackDTOs.containsKey(mediaTrackId))
                .collect(Collectors.toSet());

        if (0 < missingMediaTrackIds.size()) {
            Map<UUID, MediaTrackDTO> missingMediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(missingMediaTrackIds);
            this.mediaTrackDTOs.putAll(missingMediaTrackDTOs);
        }

        this.mediaTrackDTOs.values()
                .stream()
                .forEach(mediaTrackDTO -> this.peerConnectionIds.add(mediaTrackDTO.peerConnectionId));
        this.collectPeerConnectionDTOs();
    }

    private void collectPeerConnectionDTOs() {
        Set<UUID> missingPeerConnectionIds = this.peerConnectionIds
                .stream()
                .filter(peerConnectionId -> !this.peerConnectionDTOs.containsKey(peerConnectionId))
                .collect(Collectors.toSet());

        if (0 < missingPeerConnectionIds.size()) {
            Map<UUID, PeerConnectionDTO> missingPeerConnectionDTOs = this.hazelcastMaps.getPeerConnections().getAll(missingPeerConnectionIds);
            this.peerConnectionDTOs.putAll(missingPeerConnectionDTOs);
        }

        this.peerConnectionDTOs.values()
                .stream()
                .forEach(peerConnectionDTO -> this.clientIds.add(peerConnectionDTO.clientId));
        this.collectClientDTOs();
    }

    private void collectClientDTOs() {
        Set<UUID> missingClientIds = this.clientIds
                .stream()
                .filter(clientId -> !this.clientDTOs.containsKey(clientId))
                .collect(Collectors.toSet());

        if (0 < missingClientIds.size()) {
            Map<UUID, ClientDTO> missingClientDTOs = this.hazelcastMaps.getClients().getAll(missingClientIds);
            this.clientDTOs.putAll(missingClientDTOs);
        }

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
        Map<UUID, List<CallEventReport.Builder>> buildersByTrackIds =
                this.callEventReportBuilders.stream()
                        .filter(builder -> {
                            if (UUIDAdapter.tryParse(builder.getMediaTrackId()).isEmpty()) {
                                logger.warn("Cannot parse track id of for report {} Report will not be built", builder.toString());
                                return false;
                            }
                            return true;
                        })
                        .collect(groupingBy(callEventReport -> UUID.fromString(callEventReport.getMediaTrackId())));

        this.mediaTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
            var reportBuilders = buildersByTrackIds.get(trackId);
            if (Objects.isNull(reportBuilders)) {
                return;
            }
            reportBuilders.stream().forEach(reportBuilder -> {
                reportBuilder
                        .setPeerConnectionId(mediaTrackDTO.peerConnectionId.toString())
                        .setMediaTrackId(trackId.toString())
                        .setSSRC(mediaTrackDTO.ssrc)
                ;

            });
        });
        prepareBuildersByPeerConnections();
    }

    private void prepareBuildersByPeerConnections() {
        Map<UUID, List<CallEventReport.Builder>> buildersByPeerConnectionIds =
                this.callEventReportBuilders.stream()
                        .filter(builder -> {
                            if (UUIDAdapter.tryParse(builder.getPeerConnectionId()).isEmpty()) {
                                logger.warn("Cannot parse peer connection id of for report {} Report will not be built", builder.toString());
                                return false;
                            }
                            return true;
                        })
                        .collect(groupingBy(callEventReport -> UUID.fromString(callEventReport.getPeerConnectionId())));

        this.peerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
            var reportBuilders = buildersByPeerConnectionIds.get(peerConnectionId);
            if (Objects.isNull(reportBuilders)) {
                return;
            }
            reportBuilders.stream().forEach(reportBuilder -> {
                reportBuilder
                        .setClientId(peerConnectionDTO.clientId.toString())
                        .setPeerConnectionId(peerConnectionId.toString())
                ;

            });
        });
        prepareBuildersByClients();
    }

    private void prepareBuildersByClients() {
        Map<UUID, List<CallEventReport.Builder>> buildersByClientIds =
                this.callEventReportBuilders.stream()
                        .filter(builder -> {
                            if (UUIDAdapter.tryParse(builder.getClientId()).isEmpty()) {
                                logger.warn("Cannot parse client id of for report {} Report will not be built", builder.toString());
                                return false;
                            }
                            return true;
                        })
                        .collect(groupingBy(callEventReport -> UUID.fromString(callEventReport.getClientId())));

        this.clientDTOs.forEach((clientId, clientDTO) -> {
            var reportBuilders = buildersByClientIds.get(clientId);
            if (Objects.isNull(reportBuilders)) {
                return;
            }
            reportBuilders.stream().forEach(reportBuilder -> {
                reportBuilder
                        .setCallId(clientDTO.callId.toString())
                        .setClientId(clientId.toString())
                        .setMediaUnitId(clientDTO.mediaUnitId)
                        .setUserId(clientDTO.userId)
                ;

            });
        });
        prepareBuildersByCalls();
    }

    private void prepareBuildersByCalls() {
        Map<UUID, List<CallEventReport.Builder>> buildersByCallIds =
                this.callEventReportBuilders.stream()
                        .filter(builder -> {
                            if (UUIDAdapter.tryParse(builder.getCallId()).isEmpty()) {
                                logger.warn("Cannot parse call id of for report {} Report will not be built", builder.toString());
                                return false;
                            }
                            return true;
                        })
                        .collect(groupingBy(callEventReport -> UUID.fromString(callEventReport.getCallId())));

        this.callDTOs.forEach((callId, callDTO) -> {
            var reportBuilders = buildersByCallIds.get(callId);
            if (Objects.isNull(reportBuilders)) {
                return;
            }
            reportBuilders.stream().forEach(reportBuilder -> {
                reportBuilder
                        .setServiceId(callDTO.serviceId)
                        .setRoomId(callDTO.roomId)
                        .setCallId(callId.toString())
                ;
            });
        });
    }

    private Optional<CallEventReport> buildReport(CallEventReport.Builder builder) {
        try {
            CallEventReport report = builder
                    .setName(this.callEventType.name())
                    .setMessage(this.message)
                    .build();
            return Optional.of(report);
        } catch (Exception ex) {
            logger.error("Cannot make report.", ex);
            return Optional.empty();
        }

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
        UUID callId = UUID.fromString(builder.getCallId());
        Long timestamp = this.explicitTimestamps.getOrDefault(callId, Instant.now().toEpochMilli());
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
        UUID clientId = UUID.fromString(builder.getClientId());
        Long timestamp = this.explicitTimestamps.getOrDefault(clientId, Instant.now().toEpochMilli());
        return builder
                .setTimestamp(timestamp);
    }

    private CallEventReport.Builder setupPeerConnectionOpenedReport(CallEventReport.Builder builder) {
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
        UUID peerConnectionId = UUID.fromString(builder.getPeerConnectionId());
        Long timestamp = this.explicitTimestamps.getOrDefault(peerConnectionId, Instant.now().toEpochMilli());
        return builder
                .setTimestamp(timestamp);
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
        UUID trackId = UUID.fromString(builder.getMediaTrackId());
        Long timestamp = this.explicitTimestamps.getOrDefault(trackId, Instant.now().toEpochMilli());
        return builder
                .setTimestamp(timestamp);
    }

    private void addCallDTO(CallDTO value) {
        String callId = value.callId.toString();
        this.callEventReportBuilders.add(
                CallEventReport.newBuilder().setCallId(callId)
        );
        this.callDTOs.put(value.callId, value);
    }

    private void addClientDTO(ClientDTO value) {
        String clientId = value.clientId.toString();
        this.callEventReportBuilders.add(
                CallEventReport.newBuilder().setClientId(clientId)
        );
        this.clientDTOs.put(value.clientId, value);
    }

    private void addPeerConnectionDTO(PeerConnectionDTO value) {
        String peerConnectionId = value.peerConnectionId.toString();
        this.callEventReportBuilders.add(
                CallEventReport.newBuilder().setPeerConnectionId(peerConnectionId)
        );
        this.peerConnectionDTOs.put(value.peerConnectionId, value);
    }

    private void addMediaTrackDTO(MediaTrackDTO value) {
        String trackId = value.trackId.toString();
        this.callEventReportBuilders.add(
                CallEventReport.newBuilder().setMediaTrackId(trackId)
        );
        this.mediaTrackDTOs.put(value.trackId, value);
    }
}
