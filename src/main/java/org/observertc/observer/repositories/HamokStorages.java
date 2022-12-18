package org.observertc.observer.repositories;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HamokStorages {
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
    SfusRepository sfusRepository;

    @Inject
    SfuTransportsRepository sfuTransportsRepository;

    @Inject
    SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;

    @Inject
    SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;

    @Inject
    SfuSctpChannelsRepository sfuSctpStreamsRepository;

    public CallsRepository getCallsRepository() {
        return callsRepository;
    }

    public ClientsRepository getClientsRepository() {
        return clientsRepository;
    }

    public PeerConnectionsRepository getPeerConnectionsRepository() {
        return peerConnectionsRepository;
    }

    public InboundTracksRepository getInboundTracksRepository() {
        return inboundTracksRepository;
    }

    public OutboundTracksRepository getOutboundTracksRepository() {
        return outboundTracksRepository;
    }

    public SfusRepository getSfusRepository() {
        return sfusRepository;
    }

    public SfuTransportsRepository getSfuTransportsRepository() {
        return sfuTransportsRepository;
    }

    public SfuInboundRtpPadsRepository getSfuInboundRtpPadsRepository() {
        return sfuInboundRtpPadsRepository;
    }

    public SfuOutboundRtpPadsRepository getSfuOutboundRtpPadsRepository() {
        return sfuOutboundRtpPadsRepository;
    }

    public SfuSctpChannelsRepository getSfuSctpStreamsRepository() {
        return sfuSctpStreamsRepository;
    }

}
