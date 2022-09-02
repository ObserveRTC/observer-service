package org.observertc.observer.evaluators.eventreports;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Singleton;
import org.observertc.observer.events.SfuEventType;
import org.observertc.schemas.dtos.Models;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class SfuSctpStreamRemovedReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuSctpStreamRemovedReports.class);

    private Subject<List<SfuEventReport>> output = PublishSubject.<List<SfuEventReport>>create().toSerialized();

    @PostConstruct
    void setup() {

    }

    public void accept(List<Models.SfuSctpStream> sctpStreamModels) {
        if (Objects.isNull(sctpStreamModels) || sctpStreamModels.size() < 1) {
            return;
        }
        var reports = sctpStreamModels.stream()
                .map(this::makeReport)
                .collect(Collectors.toList());

        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }

    private SfuEventReport makeReport(Models.SfuSctpStream sctpStreamModel) {
        try {
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_SCTP_STREAM_REMOVED.name())
                    .setSfuId(sctpStreamModel.getSfuId())
//                    .setCallId(callId)
                    .setTransportId(sctpStreamModel.getSfuTransportId())
                    .setSctpStreamId(sctpStreamModel.getSfuSctpStreamId())
                    .setMessage("Sfu SCTP stream is removed")
                    .setServiceId(sctpStreamModel.getServiceId())
                    .setMediaUnitId(sctpStreamModel.getMediaUnitId())
                    .setTimestamp(sctpStreamModel.getOpened())
                    .setMarker(sctpStreamModel.getMarker())
                    ;
            logger.info("SFU SCTP Stream (id: {}) is REMOVED (mediaUnitId: {}, serviceId {}).",
                    sctpStreamModel.getSfuSctpStreamId(),
                    sctpStreamModel.getMediaUnitId(),
                    sctpStreamModel.getServiceId()
            );
            return builder.build();
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }

    public Observable<List<SfuEventReport>> getOutput() {
        return this.output;
    }
}
