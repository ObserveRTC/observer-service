package org.observertc.webrtc.observer.evaluators.rtpmonitors;

import com.hazelcast.map.IMap;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.dto.RemoteInboundRtpTrafficDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.RemoteInboundRTP;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Objects;
import java.util.function.Supplier;

@Singleton
public class RemoteInboundRtpMonitor extends RtpMonitorAbstract<RemoteInboundRTP> {
    private static final Logger logger = LoggerFactory.getLogger(RemoteInboundRtpMonitor.class);

    private final IMap<String, RemoteInboundRtpTrafficDTO> remoteInboundRtpDTOs;
    private final double avgFactor;

    public RemoteInboundRtpMonitor(HazelcastMaps hazelcastMaps, ObserverConfig.RemoteInboundRtpMonitorConfig config) {
        super(config);
        this.remoteInboundRtpDTOs = hazelcastMaps.getRemoteInboundTrafficDTOs();
        this.avgFactor = config.weightFactor;
    }

    @Override
    protected RemoteInboundRTP getPayload(Report report) {
        return (RemoteInboundRTP) report.getPayload();
    }

    @Override
    protected String getPayloadKey(RemoteInboundRTP payload) {
        return getKey(payload.getPeerConnectionUUID(), payload.getSsrc());
    }

    @Override
    protected void doMonitor(RemoteInboundRTP payload, String payloadKey) {
        RemoteInboundRtpTrafficDTO inboundRtpTrafficDTO = this.getOrBuild(payloadKey, () ->
                RemoteInboundRtpTrafficDTO.builder()
                        .withPeerConnectionUUID(payload.getPeerConnectionUUID())
                        .withSSRC(payload.getSsrc())
                        .build()
        );
        this.updateRttAvg(inboundRtpTrafficDTO, payload.getRoundTripTime());
        this.remoteInboundRtpDTOs.put(payloadKey, inboundRtpTrafficDTO);
    }

    @Override
    protected void remove(String key) {
        this.remoteInboundRtpDTOs.remove(key);
    }

    private RemoteInboundRtpTrafficDTO getOrBuild(String key, Supplier<RemoteInboundRtpTrafficDTO> builder) {
        RemoteInboundRtpTrafficDTO result = this.remoteInboundRtpDTOs.get(key);
        if (Objects.nonNull(result)) {
            return result;
        }
        return builder.get();
    }

    private void updateRttAvg(RemoteInboundRtpTrafficDTO remoteInboundRtpTrafficDTO, Double roundTripTimeInS) {
        if (Objects.isNull(roundTripTimeInS) || roundTripTimeInS < 0.) {
            return;
        }
        if (remoteInboundRtpTrafficDTO.rttAvg < 0.) {
            remoteInboundRtpTrafficDTO.rttAvg = roundTripTimeInS;
        } else {
            remoteInboundRtpTrafficDTO.rttAvg = remoteInboundRtpTrafficDTO.rttAvg * this.avgFactor + roundTripTimeInS * (1.-this.avgFactor);
        }
    }

}
