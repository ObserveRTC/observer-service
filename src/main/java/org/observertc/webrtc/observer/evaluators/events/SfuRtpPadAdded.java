package org.observertc.webrtc.observer.evaluators.events;

import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.dto.SfuRtpPadDTO;
import org.observertc.webrtc.observer.repositories.RepositoryUpdateEvent;
import org.observertc.webrtc.observer.repositories.tasks.sync.SyncTask;
import org.observertc.webrtc.schemas.reports.SfuEventReport;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;

@Singleton
//public class ClientsJoined extends Observable<CallEventReport> {
public class SfuRtpPadAdded {

    private Subject<SfuEventReport> reports = PublishSubject.create();

    @Inject
    Provider<SyncTask<SfuRtpPadDTO>> syncTask;

    @PostConstruct
    void setup() {
    }

    void receiveAddedSfuRtpPads(List<SfuRtpPadDTO> sfuRtpPadDTOs) {
        if (Objects.isNull(sfuRtpPadDTOs) || sfuRtpPadDTOs.size() < 1) {
            return;
        }
        sfuRtpPadDTOs.stream()
                .filter(sfuRtpPadDTO -> Objects.nonNull(sfuRtpPadDTO.callId))
                .map(this::makeSfuRtpPadAddedReportBuilder)
                .forEach(report -> {
                    reports.onNext(report);
                });
    }

    void receiveUpdatedSfuRtpPads(List<RepositoryUpdateEvent<SfuRtpPadDTO>> updatedSfuRtpPadDTOs) {
        if (Objects.isNull(updatedSfuRtpPadDTOs) || updatedSfuRtpPadDTOs.size() < 1) {
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
                .map(this::makeSfuRtpPadAddedReportBuilder)
                .forEach(report -> {
                    reports.onNext(report);
                });
    }

    private SfuEventReport makeSfuRtpPadAddedReportBuilder(SfuRtpPadDTO sfuRtpPadDTO) {
        String sfuPadId = Objects.nonNull(sfuRtpPadDTO.sfuPadId) ?  sfuRtpPadDTO.sfuPadId.toString() : null;
        String sfuPadStreamDirection = Objects.nonNull(sfuRtpPadDTO.streamDirection) ? sfuRtpPadDTO.streamDirection.toString() : "Unknown";
        var builder = SfuEventReport.newBuilder()
                .setName(SfuEventType.SFU_RTP_PAD_ADDED.name())
                .setSfuId(sfuRtpPadDTO.sfuId.toString())
                .setCallId(sfuRtpPadDTO.callId.toString())
                .setTransportId(sfuRtpPadDTO.sfuTransportId.toString())
                .setRtpStreamId(sfuRtpPadDTO.rtpStreamId.toString())
                .setSfuPadId(sfuPadId)
                .setAttachments("Direction of the Rtp stream is: " + sfuPadStreamDirection)
                .setMessage("Sfu Rtp Pad is added")
                .setMediaUnitId(sfuRtpPadDTO.mediaUnitId)
                .setTimestamp(sfuRtpPadDTO.added);
        return builder.build();
    }
}
