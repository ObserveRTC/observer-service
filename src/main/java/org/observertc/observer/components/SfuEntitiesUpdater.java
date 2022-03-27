package org.observertc.observer.components;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.components.depots.SfuDTOsDepot;
import org.observertc.observer.components.depots.SfuRtpPadDTOsDepot;
import org.observertc.observer.components.depots.SfuTransportDTOsDepot;
import org.observertc.observer.dto.SfuDTO;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.repositories.tasks.AddSFUsTask;
import org.observertc.observer.repositories.tasks.AddSfuRtpPadsTask;
import org.observertc.observer.repositories.tasks.AddSfuTransportsTask;
import org.observertc.observer.repositories.tasks.RefreshSfusTask;
import org.observertc.observer.samples.ObservedSfuSamples;
import org.observertc.observer.samples.SfuSampleVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Prototype
public class SfuEntitiesUpdater implements Consumer<ObservedSfuSamples> {
    private static final Logger logger = LoggerFactory.getLogger(SfuEntitiesUpdater.class);

    @Inject
    Provider<RefreshSfusTask> refreshSfusTaskProvider;

    @Inject
    Provider<AddSFUsTask> addSFUsTaskProvider;

    @Inject
    Provider<AddSfuTransportsTask> addSfuTransportsTaskProvider;

    @Inject
    Provider<AddSfuRtpPadsTask> addSfuRtpPadsTaskProvider;

    private Subject<ObservedSfuSamples> output = PublishSubject.create();
    private final SfuDTOsDepot sfuDTOsDepot = new SfuDTOsDepot();
    private final SfuTransportDTOsDepot sfuTransportDTOsDepot = new SfuTransportDTOsDepot();
    private final SfuRtpPadDTOsDepot sfuRtpPadDTOsDepot = new SfuRtpPadDTOsDepot();

    public Observable<ObservedSfuSamples> observableClientSamples() {
        return this.output;
    }


    public void accept(ObservedSfuSamples observedSfuSamples) {
        if (observedSfuSamples.isEmpty()) {
            return;
        }
        var findDTOs = this.refreshSfusTaskProvider.get()
                .withSfuIds(observedSfuSamples.getSfuIds())
                .withSfuTransportIds(observedSfuSamples.getTransportIds())
                .withSfuRtpPadIds(observedSfuSamples.getRtpPadIds())
                ;
        if (!findDTOs.execute().succeeded()) {
            logger.warn("Interrupted execution of component due to unsuccessful task execution");
            return;
        }
        var findDTOsTaskResult = findDTOs.getResult();
        var foundSfuIds = findDTOsTaskResult.foundSfuIds;
        var foundTransportIds = findDTOsTaskResult.foundSfuTransportIds;
        var foundRtpPadIds = findDTOsTaskResult.foundRtpPadIds;
        for (var observedSfuSample : observedSfuSamples) {
            var sfuSample = observedSfuSample.getSfuSample();
            if (!foundSfuIds.contains(sfuSample.sfuId)) {
                sfuDTOsDepot.addFromObservedClientSample(observedSfuSample);
            }
            SfuSampleVisitor.streamTransports(sfuSample).forEach(sfuTransport -> {
                if (foundTransportIds.contains(sfuTransport.transportId)) return;
                this.sfuTransportDTOsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setSfuTransport(sfuTransport)
                        .assemble()
                ;
            });
            SfuSampleVisitor.streamInboundRtpPads(sfuSample).forEach(sfuInboundRtpPad -> {
                if (foundRtpPadIds.contains(sfuInboundRtpPad.padId)) return;
                this.sfuRtpPadDTOsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setSfuInboundRtpPad(sfuInboundRtpPad)
                        .assemble()
                ;
            });
            SfuSampleVisitor.streamOutboundRtpPads(sfuSample).forEach(sfuOutboundRtpPad -> {
                if (foundRtpPadIds.contains(sfuOutboundRtpPad.padId)) return;
                this.sfuRtpPadDTOsDepot
                        .setObservedSfuSample(observedSfuSample)
                        .setOutboundRtpPad(sfuOutboundRtpPad)
                        .assemble()
                ;
            });
        }

        var newSfuDTOs = this.sfuDTOsDepot.get();
        if (0 < newSfuDTOs.size()) {
            this.addNewSfus(newSfuDTOs);
        }
        var newSfuTransportDTOs = this.sfuTransportDTOsDepot.get();
        if (0 < newSfuTransportDTOs.size()) {
            this.addNewTransports(newSfuTransportDTOs);
        }
        var newSfuRtpPadDTOs = this.sfuRtpPadDTOsDepot.get();
        if (0 < newSfuRtpPadDTOs.size()) {
            this.addNewRtpPads(newSfuRtpPadDTOs);
        }
        if (0 < observedSfuSamples.size()) {
            synchronized (this) {
                this.output.onNext(observedSfuSamples);
            }
        }

    }

    private void addNewSfus(Map<UUID, SfuDTO> DTOs) {
        var task = addSFUsTaskProvider.get()
                .withSfuDTOs(DTOs)
                ;

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return;
        }
    }

    private void addNewTransports(Map<UUID, SfuTransportDTO> DTOs) {
        var task = addSfuTransportsTaskProvider.get()
                .withSfuTransportDTOs(DTOs)
                ;
        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return;
        }
    }

    private void addNewRtpPads(Map<UUID, SfuRtpPadDTO> DTOs) {
        var task = addSfuRtpPadsTaskProvider.get()
                .withSfuRtpPadDTOs(DTOs);

        if (!task.execute().succeeded()) {
            logger.warn("{} task execution failed, repository may become inconsistent!", task.getClass().getSimpleName());
            return;
        }
    }

}
