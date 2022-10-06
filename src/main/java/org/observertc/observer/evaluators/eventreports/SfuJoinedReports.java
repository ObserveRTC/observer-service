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
public class SfuJoinedReports {

    private static final Logger logger = LoggerFactory.getLogger(SfuJoinedReports.class);

    private Subject<List<SfuEventReport>> output = PublishSubject.<List<SfuEventReport>>create().toSerialized();

    @PostConstruct
    void setup() {

    }

    public void accept(List<Models.Sfu> sfuDTOs) {
        if (Objects.isNull(sfuDTOs) || sfuDTOs.size() < 1) {
            return;
        }
        var reports = sfuDTOs.stream()
                .map(this::makeReport)
                .collect(Collectors.toList());

        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }


    protected SfuEventReport makeReport(Models.Sfu sfuDTO) {
        try {
            var builder = SfuEventReport.newBuilder()
                    .setName(SfuEventType.SFU_JOINED.name())
                    .setSfuId(sfuDTO.getSfuId())
                    .setMessage("Sfu is joined")
                    .setServiceId(sfuDTO.getServiceId())
                    .setMediaUnitId(sfuDTO.getMediaUnitId())
                    .setTimestamp(sfuDTO.getJoined())
                    .setMarker(sfuDTO.getMarker())
                    ;
            logger.info("SFU (sfuId: {}, mediaUnitId: {}) is JOINED. serviceId: {}.",
                    sfuDTO.getSfuId(),
                    sfuDTO.getMediaUnitId(),
                    sfuDTO.getServiceId()
            );
            return builder.build();
        } catch (Exception ex) {
            logger.error("Cannot make report for Sfu DTO", ex);
            return null;
        }
    }

    public Observable<List<SfuEventReport>> getOutput() {
        return this.output;
    }
}
