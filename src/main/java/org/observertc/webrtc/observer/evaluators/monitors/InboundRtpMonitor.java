package org.observertc.webrtc.observer.evaluators.monitors;

import com.hazelcast.map.IMap;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.dto.InboundRtpTrafficDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.InboundRTP;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Objects;
import java.util.function.Supplier;

@Singleton
public class InboundRtpMonitor extends RtpMonitorAbstract<InboundRTP> {
    private static final Logger logger = LoggerFactory.getLogger(InboundRtpMonitor.class);

    private final IMap<String, InboundRtpTrafficDTO> inboundRtpDTOs;

    public InboundRtpMonitor(HazelcastMaps hazelcastMaps, ObserverConfig.InboundRtpMonitorConfig config) {
        super(config);
        this.inboundRtpDTOs = hazelcastMaps.getInboundRtpDTOs();
    }
    @Override
    protected InboundRTP getPayload(Report report) {
        return (InboundRTP) report.getPayload();
    }

    @Override
    protected String getPayloadKey(InboundRTP payload) {
        return getKey(payload.getPeerConnectionUUID(), payload.getSsrc());
    }

    @Override
    protected void doMonitor(InboundRTP payload, String payloadKey) {
        InboundRtpTrafficDTO inboundRtpTrafficDTO = this.getOrBuild(payloadKey, () ->
                InboundRtpTrafficDTO.builder()
                        .withPeerConnectionUUID(payload.getPeerConnectionUUID())
                        .withSSRC(payload.getSsrc())
                        .build()
        );
        this.updateReceivedBytes(inboundRtpTrafficDTO, payload.getBytesReceived());
        this.updateReceivedPackets(inboundRtpTrafficDTO, payload.getPacketsReceived());
        this.updateLostPackets(inboundRtpTrafficDTO, payload.getPacketsLost());
        this.inboundRtpDTOs.put(payloadKey, inboundRtpTrafficDTO);
    }

    @Override
    protected void remove(String key) {
        this.inboundRtpDTOs.remove(key);
    }

    private InboundRtpTrafficDTO getOrBuild(String key, Supplier<InboundRtpTrafficDTO> builder) {
        InboundRtpTrafficDTO result = this.inboundRtpDTOs.get(key);
        if (Objects.nonNull(result)) {
            return result;
        }
        return builder.get();
    }

    private void updateReceivedBytes(InboundRtpTrafficDTO inboundRtpTrafficDTO, Long bytesReceived) {
        if (Objects.isNull(bytesReceived) || bytesReceived < 0) {
            return;
        }
        if (inboundRtpTrafficDTO.firstBytesReceived < 0 || bytesReceived < inboundRtpTrafficDTO.lastBytesReceived) {
            // either it is started now, or reset happened.
            inboundRtpTrafficDTO.firstBytesReceived = inboundRtpTrafficDTO.lastBytesReceived = bytesReceived;
        } else {
            inboundRtpTrafficDTO.lastBytesReceived = bytesReceived;
        }
    }

    private void updateReceivedPackets(InboundRtpTrafficDTO inboundRtpTrafficDTO, Integer packetsReceived) {
        if (Objects.isNull(packetsReceived) || packetsReceived < 0) {
            return;
        }
        if (inboundRtpTrafficDTO.firstPacketsReceived < 0 || packetsReceived < inboundRtpTrafficDTO.lastPacketsReceived) {
            // either it is started now, or reset happened.
            inboundRtpTrafficDTO.firstPacketsReceived = inboundRtpTrafficDTO.lastPacketsReceived = packetsReceived;
        } else {
            inboundRtpTrafficDTO.lastPacketsReceived = packetsReceived;
        }
    }

    private void updateLostPackets(InboundRtpTrafficDTO inboundRtpTrafficDTO, Integer packetsLost) {
        if (Objects.isNull(packetsLost) || packetsLost < 0) {
            return;
        }
        if (inboundRtpTrafficDTO.firstPacketsLost < 0 || packetsLost < inboundRtpTrafficDTO.lastPacketsLost) {
            // either it is started now, or reset happened.
            inboundRtpTrafficDTO.firstPacketsLost = inboundRtpTrafficDTO.lastPacketsLost = packetsLost;
        } else {
            inboundRtpTrafficDTO.firstPacketsLost = packetsLost;
        }
    }


}
