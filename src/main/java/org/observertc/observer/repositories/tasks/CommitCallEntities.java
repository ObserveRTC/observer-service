package org.observertc.observer.repositories.tasks;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.metrics.EvaluatorMetrics;
import org.observertc.observer.repositories.*;

import java.time.Instant;

@Singleton
public class CommitCallEntities extends CommitAbstract{

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    PeerConnectionsRepository peerConnectionsRepository;

    @Inject
    InboundTracksRepository inboundTracksRepository;

    @Inject
    OutboundTracksRepository outboundTracksRepository;

    @Inject
    EvaluatorMetrics evaluatorMetrics;

    public CommitCallEntities() {
        super(
                "Committing CallEntities",
                60000
        );
    }

    @Override
    protected void process() throws Throwable {
        Instant started = Instant.now();
        this.inboundTracksRepository.save();
        this.outboundTracksRepository.save();
        this.peerConnectionsRepository.save();
        this.clientsRepository.save();
        this.callsRepository.save();
        Instant ended = Instant.now();
        this.evaluatorMetrics.addCommitCallEntitiesExecutionTime(started, ended);
    }
}
