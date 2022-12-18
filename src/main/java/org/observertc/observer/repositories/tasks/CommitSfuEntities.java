package org.observertc.observer.repositories.tasks;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.metrics.EvaluatorMetrics;
import org.observertc.observer.repositories.*;

import java.time.Instant;

@Singleton
public class CommitSfuEntities extends CommitAbstract{

    @Inject
    SfusRepository sfusRepository;

    @Inject
    SfuTransportsRepository sfuTransportsRepository;

    @Inject
    SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;

    @Inject
    SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;

    @Inject
    SfuSctpChannelsRepository sfuSctpChannelsRepository;

    @Inject
    EvaluatorMetrics evaluatorMetrics;

    public CommitSfuEntities() {
        super(
                "Committing SfuEntities",
                60000
        );
    }

    @Override
    protected void process() throws Throwable {
        Instant started = Instant.now();
        this.sfuSctpChannelsRepository.save();
        this.sfuInboundRtpPadsRepository.save();
        this.sfuOutboundRtpPadsRepository.save();
        this.sfuTransportsRepository.save();
        this.sfusRepository.save();
        Instant ended = Instant.now();
        this.evaluatorMetrics.addCommitSfuEntitiesExecutionTime(started, ended);
    }
}
