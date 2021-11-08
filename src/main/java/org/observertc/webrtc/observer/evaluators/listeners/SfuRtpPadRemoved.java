package org.observertc.webrtc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.SfuRtpPadDTO;
import org.observertc.webrtc.observer.repositories.RepositoryEvents;
import org.observertc.webrtc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.webrtc.observer.repositories.tasks.RemoveSfuRtpPadsTask;
import org.observertc.webrtc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.*;

@Prototype
class SfuRtpPadRemoved extends EventReporterAbstract.SfuEventReporterAbstract<SfuRtpPadDTO> {

    private static final Logger logger = LoggerFactory.getLogger(SfuRtpPadRemoved.class);

    @Inject
    ObserverConfig observerConfig;

    @Inject
    RepositoryEvents repositoryEvents;

    @Inject
    Provider<RemoveSfuRtpPadsTask> removeSfuRtpPadsTaskProvider;

    @PostConstruct
    void setup() {
        this.repositoryEvents.removedSfuRtpPads()
                .subscribe(this::receiveRemovedSfuRtpPads);

        this.repositoryEvents.expiredSfuRtpPads()
                .subscribe(this::receiveExpiredSfuRtpPad);

    }

    private void receiveRemovedSfuRtpPads(List<SfuRtpPadDTO> sfuRtpPadDTOs) throws Throwable{
        if (Objects.isNull(sfuRtpPadDTOs) || sfuRtpPadDTOs.size() < 1) {
            return;
        }
                // this is just acknowledge the removal, as in normal cases it is expired, which
        // triggers the removal task from here, but if a higher level ordered the purge,
        // we just need to report it.
        sfuRtpPadDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .forEach(this::forward);
    }

    void receiveExpiredSfuRtpPad(List<RepositoryExpiredEvent<SfuRtpPadDTO>> expiredSfuRtpPads) throws Throwable {
        if (Objects.isNull(expiredSfuRtpPads) || expiredSfuRtpPads.size() < 1) {
            return;
        }
        // this triggers a removeSfuRtpPad
        var task = this.removeSfuRtpPadsTaskProvider.get();
        Map<UUID, Long> estimatedRemovals = new HashMap<>();
        expiredSfuRtpPads.stream().forEach(expiredSfuRtpPad -> {
            var sfuRtpPadDTO = expiredSfuRtpPad.getValue();
            var estimatedRemoval = expiredSfuRtpPad.estimatedLastTouch();
            estimatedRemovals.put(sfuRtpPadDTO.sfuPadId, estimatedRemoval);
            task.addRemovedSfuRtpStreamPadDTO(sfuRtpPadDTO);
        });

        if (!task.execute().succeeded()) {
            logger.warn("Removing expired SfuRtpPad was unsuccessful");
            // we still need to report about the removal
            return;
        }
        task.getResult().stream().map(removedRtpPad -> {
            Long estimatedRemoval = estimatedRemovals.getOrDefault(removedRtpPad.sfuPadId, Instant.now().toEpochMilli());
            var report = this.makeReport(removedRtpPad, estimatedRemoval);
            return report;
        }).filter(Objects::nonNull).forEach(this::forward);
    }

    private SfuEventReport makeReport(SfuRtpPadDTO sfuRtpPadDTO){
        Long timestamp = Instant.now().toEpochMilli();
        return this.makeReport(sfuRtpPadDTO, timestamp);
    }

    @Override
    protected SfuEventReport makeReport(SfuRtpPadDTO sfuRtpPadDTO, Long timestamp) {
        try {
            String callId = UUIDAdapter.toStringOrNull(sfuRtpPadDTO.callId);
            String sfuPadId = Objects.nonNull(sfuRtpPadDTO.sfuPadId) ?  sfuRtpPadDTO.sfuPadId.toString() : null;
            String sfuPadStreamDirection = Objects.nonNull(sfuRtpPadDTO.streamDirection) ? sfuRtpPadDTO.streamDirection.toString() : "Unknown";
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_RTP_PAD_REMOVED.name())
                    .setSfuId(sfuRtpPadDTO.sfuId.toString())
                    .setCallId(callId)
                    .setTransportId(sfuRtpPadDTO.sfuTransportId.toString())
                    .setRtpStreamId(sfuRtpPadDTO.rtpStreamId.toString())
                    .setSfuPadId(sfuPadId)
                    .setAttachments("Direction of the Rtp stream is: " + sfuPadStreamDirection)
                    .setMessage("Sfu Rtp Pad is removed")
                    .setServiceId(sfuRtpPadDTO.serviceId)
                    .setMediaUnitId(sfuRtpPadDTO.mediaUnitId)
                    .setTimestamp(timestamp);
            logger.info("SFU Pad (id: {}, rtpStreamId: {}) is REMOVED (mediaUnitId: {}, serviceId {}), direction is {}",
                    sfuPadId, sfuRtpPadDTO.rtpStreamId, sfuRtpPadDTO.mediaUnitId, sfuRtpPadDTO.serviceId, sfuPadStreamDirection
            );
            return builder.build();
        } catch (Exception ex) {
            logger.warn("Exception while making report", ex);
            return null;
        }
    }
}
