package org.observertc.webrtc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.common.Utils;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.SfuRtpPadDTO;
import org.observertc.webrtc.observer.evaluators.listeners.attachments.RtpPadAttachment;
import org.observertc.webrtc.observer.repositories.EtcMap;
import org.observertc.webrtc.observer.repositories.RepositoryEvents;
import org.observertc.webrtc.observer.repositories.RepositoryUpdatedEvent;
import org.observertc.webrtc.observer.repositories.tasks.QueryTask;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Prototype
class SfuRtpPadAdded extends EventReporterAbstract.SfuEventReporterAbstract<SfuRtpPadDTO> {

    private static final Logger logger = LoggerFactory.getLogger(SfuRtpPadAdded.class);

    private int reportSfuRtpPadWithCallIdTimeoutInS = 60;

    @Inject
    ObserverConfig observerConfig;

    @Inject
    RepositoryEvents repositoryEvents;

    @Inject
    EtcMap etcMap;

    @Inject
    Provider<QueryTask<List<SfuRtpPadDTO>>> querySfuRtpPadDTOs;

    @PostConstruct
    void setup() {
        this.repositoryEvents
                .addedSfuRtpPads()
                .subscribe(this::receiveAddedSfuRtpPads);

        this.repositoryEvents
                .updatedSfuRtpPads()
                .subscribe(this::receiveUpdatedSfuRtpPads);

        this.repositoryEvents
                .expiredIncompleteSfuRtpPadIds()
                .subscribe(this::receiveExpiredIncompleteSfuRtpPadIds);

        this.reportSfuRtpPadWithCallIdTimeoutInS = observerConfig.evaluators.reportSfuRtpPadWithCallIdTimeoutInS;
    }

    private void receiveAddedSfuRtpPads(List<SfuRtpPadDTO> sfuRtpPadDTOs) {
        if (Objects.isNull(sfuRtpPadDTOs) || sfuRtpPadDTOs.size() < 1) {
            return;
        }

        sfuRtpPadDTOs.stream()
                .filter(sfuRtpPadDTO -> {
                    return this.reportSfuRtpPadWithCallIdTimeoutInS < 1 ||
                            Objects.nonNull(sfuRtpPadDTO.callId) ||
                            sfuRtpPadDTO.internalPad;
                })
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .forEach(this::forward);
    }

    private void receiveExpiredIncompleteSfuRtpPadIds(List<UUID> expiredSfuRtpPadIds) {
        if (Objects.isNull(expiredSfuRtpPadIds) || expiredSfuRtpPadIds.size() < 1) {
            return;
        }
        var task = this.querySfuRtpPadDTOs.get().withQuery(hazelcastMaps -> {
            Set<UUID> localRtpPadIds = hazelcastMaps.getSFURtpPads().localKeySet().stream().filter(expiredSfuRtpPadIds::contains).collect(Collectors.toSet());
            return hazelcastMaps.getSFURtpPads().getAll(localRtpPadIds)
                    .values().stream()
                    .filter(sfuRtpPadDTO -> Objects.isNull(sfuRtpPadDTO.callId) && sfuRtpPadDTO.internalPad == false)
                    .collect(Collectors.toList());
        }).execute();

        if (!task.succeeded()) {
            return;
        }
        Utils.coalesceCollection(task.getResult()).stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .forEach(this::forward);
    }

    private void receiveUpdatedSfuRtpPads(List<RepositoryUpdatedEvent<SfuRtpPadDTO>> updatedSfuRtpPadDTOs) {
        if (Objects.isNull(updatedSfuRtpPadDTOs) || updatedSfuRtpPadDTOs.size() < 1) {
            return;
        }
        updatedSfuRtpPadDTOs.stream()
                .filter(updatedSfuRtpPadDTO -> {
                    if (Objects.isNull(updatedSfuRtpPadDTO)) return false;
                    var oldValue = updatedSfuRtpPadDTO.getOldValue();
                    var newValue = updatedSfuRtpPadDTO.getNewValue();
                    if (Objects.isNull(oldValue) || Objects.isNull(newValue)) return false;
                    return oldValue.internalPad == false &&
                            Objects.isNull(oldValue.callId) &&
                            Objects.nonNull(newValue.callId);
                })
                .map(updatedSfuRtpPadDTO -> updatedSfuRtpPadDTO.getNewValue())
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .forEach(this::forward);
    }

    private SfuEventReport makeReport(SfuRtpPadDTO sfuRtpPadDTO) {
        return this.makeReport(sfuRtpPadDTO, sfuRtpPadDTO.added);
    }

    protected SfuEventReport makeReport(SfuRtpPadDTO sfuRtpPadDTO, Long timestamp) {
        try {
            var attachment = RtpPadAttachment.builder()
                    .withStreamDirection(sfuRtpPadDTO.streamDirection)
                    .withInternal(sfuRtpPadDTO.internalPad)
                    .build().toBase64();
            String sfuPadId = Objects.nonNull(sfuRtpPadDTO.sfuPadId) ?  sfuRtpPadDTO.sfuPadId.toString() : null;
            String sfuPadStreamDirection = Objects.nonNull(sfuRtpPadDTO.streamDirection) ? sfuRtpPadDTO.streamDirection.toString() : "Unknown";
            String callId = UUIDAdapter.toStringOrNull(sfuRtpPadDTO.callId);
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_PAD_ADDED.name())
                    .setSfuId(sfuRtpPadDTO.sfuId.toString())
                    .setCallId(callId)
                    .setTransportId(sfuRtpPadDTO.sfuTransportId.toString())
                    .setRtpStreamId(sfuRtpPadDTO.rtpStreamId.toString())
                    .setSfuPadId(sfuPadId)
                    .setAttachments(attachment)
                    .setMessage("Sfu Rtp Pad is added")
                    .setServiceId(sfuRtpPadDTO.serviceId)
                    .setMediaUnitId(sfuRtpPadDTO.mediaUnitId)
                    .setTimestamp(sfuRtpPadDTO.added);
            logger.info("SFU Pad (id: {}, rtpStreamId: {}) is ADDED (mediaUnitId: {}, serviceId {}), direction is {}",
                    sfuPadId, sfuRtpPadDTO.rtpStreamId, sfuRtpPadDTO.mediaUnitId, sfuRtpPadDTO.serviceId, sfuPadStreamDirection
            );
            return builder.build();
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }

    @Override
    protected void forward(SfuEventReport report) {
        if (Objects.isNull(report) || Objects.isNull(report.getSfuPadId())) {
            return;
        }
        String key = String.format("%s-%s", report.getName(), report.getSfuPadId());
        if (this.etcMap.hasExpiringKey(key)) {
            logger.warn("Report {} has already been sent. Is the reportSfuRtpPadWithCallIdTimeoutInS config value high enough to prevent the situation of already sent report?");
            return;
        }

        super.forward(report);

        this.etcMap.addExpiringKey(key, 120000);
    }
}
