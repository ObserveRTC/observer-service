package org.observertc.webrtc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.SfuRtpPadDTO;
import org.observertc.webrtc.observer.evaluators.listeners.attachments.RtpPadAttachment;
import org.observertc.webrtc.observer.repositories.RepositoryEvents;
import org.observertc.webrtc.observer.repositories.RepositoryUpdatedEvent;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@Prototype
class SfuRtpPadAdded extends EventReporterAbstract.SfuEventReporterAbstract<SfuRtpPadDTO> {

    private static final Logger logger = LoggerFactory.getLogger(SfuRtpPadAdded.class);

    private boolean reportSfuRtpPadOnlyWithCallId = false;

    @Inject
    ObserverConfig observerConfig;

    @Inject
    RepositoryEvents repositoryEvents;

    @PostConstruct
    void setup() {
        this.repositoryEvents
                .addedSfuRtpPads()
                .subscribe(this::receiveAddedSfuRtpPads);

        this.repositoryEvents
                .updatedSfuRtpPads()
                .subscribe(this::receiveUpdatedSfuRtpPads);

        this.reportSfuRtpPadOnlyWithCallId = observerConfig.outboundReports.reportSfuRtpPadOnlyWithCallId;
    }

    private void receiveAddedSfuRtpPads(List<SfuRtpPadDTO> sfuRtpPadDTOs) {
        if (Objects.isNull(sfuRtpPadDTOs) || sfuRtpPadDTOs.size() < 1) {
            return;
        }

        sfuRtpPadDTOs.stream()
                .filter(sfuRtpPadDTO -> !this.reportSfuRtpPadOnlyWithCallId || Objects.nonNull(sfuRtpPadDTO.callId))
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .forEach(this::forward);
    }

    private void receiveUpdatedSfuRtpPads(List<RepositoryUpdatedEvent<SfuRtpPadDTO>> updatedSfuRtpPadDTOs) {
        if (Objects.isNull(updatedSfuRtpPadDTOs) || updatedSfuRtpPadDTOs.size() < 1) {
            return;
        }
        if (!this.reportSfuRtpPadOnlyWithCallId) {
            // in this case we already reported the added event
            return;
        }
        updatedSfuRtpPadDTOs.stream()
                .filter(updatedSfuRtpPadDTO -> {
                    if (Objects.isNull(updatedSfuRtpPadDTO)) return false;
                    var oldValue = updatedSfuRtpPadDTO.getOldValue();
                    var newValue = updatedSfuRtpPadDTO.getNewValue();
                    if (Objects.isNull(oldValue) || Objects.isNull(newValue)) return false;
                    return Objects.isNull(oldValue.callId) && Objects.nonNull(newValue.callId);
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
                    .build().toBase64();
            String sfuPadId = Objects.nonNull(sfuRtpPadDTO.sfuPadId) ?  sfuRtpPadDTO.sfuPadId.toString() : null;
            String sfuPadStreamDirection = Objects.nonNull(sfuRtpPadDTO.streamDirection) ? sfuRtpPadDTO.streamDirection.toString() : "Unknown";
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_PAD_ADDED.name())
                    .setSfuId(sfuRtpPadDTO.sfuId.toString())
                    .setCallId(sfuRtpPadDTO.callId.toString())
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

    public class Attachment {

    }
}
