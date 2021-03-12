package org.observertc.webrtc.observer.evaluators.rtpmonitors;

import com.hazelcast.map.IMap;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.dto.OutboundRtpTrafficDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.OutboundRTP;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Objects;
import java.util.function.Supplier;

@Singleton
public class OutboundRtpMonitor extends RtpMonitorAbstract<OutboundRTP> {
    private static final Logger logger = LoggerFactory.getLogger(OutboundRtpMonitor.class);

    private final IMap<String, OutboundRtpTrafficDTO> outboundRtpDTOs;

    public OutboundRtpMonitor(HazelcastMaps hazelcastMaps, ObserverConfig.InboundRtpMonitorConfig config) {
        super(config);
        this.outboundRtpDTOs = hazelcastMaps.getOutboundRtpDTOs();
    }
    @Override
    protected OutboundRTP getPayload(Report report) {
        return (OutboundRTP) report.getPayload();
    }

    @Override
    protected String getPayloadKey(OutboundRTP payload) {
        return getKey(payload.getPeerConnectionUUID(), payload.getSsrc());
    }

    @Override
    protected void doMonitor(OutboundRTP payload, String payloadKey) {
        OutboundRtpTrafficDTO outboundRtpTrafficDTO = this.getOrBuild(payloadKey, () ->
                OutboundRtpTrafficDTO.builder()
                        .withPeerConnectionUUID(payload.getPeerConnectionUUID())
                        .withSSRC(payload.getSsrc())
                        .build()
        );

        this.updateSentBytes(outboundRtpTrafficDTO, payload.getBytesSent());
        this.updateSentPackets(outboundRtpTrafficDTO, payload.getPacketsSent());
        this.outboundRtpDTOs.put(payloadKey, outboundRtpTrafficDTO);
    }

    @Override
    protected void remove(String key) {
        this.outboundRtpDTOs.remove(key);
    }

    private OutboundRtpTrafficDTO getOrBuild(String key, Supplier<OutboundRtpTrafficDTO> builder) {
        OutboundRtpTrafficDTO result = this.outboundRtpDTOs.get(key);
        if (Objects.nonNull(result)) {
            return result;
        }
        return builder.get();
    }

    private void updateSentBytes(OutboundRtpTrafficDTO outboundRtpTrafficDTO, Long bytesSent) {
        if (Objects.isNull(bytesSent) || bytesSent < 0) {
            return;
        }
        if (outboundRtpTrafficDTO.firstBytesSent < 0 || bytesSent < outboundRtpTrafficDTO.lastBytesSent) {
            // either it is started now, or reset happened.
            outboundRtpTrafficDTO.firstBytesSent = outboundRtpTrafficDTO.lastBytesSent = bytesSent;
        } else {
            outboundRtpTrafficDTO.lastBytesSent = bytesSent;
        }
    }

    private void updateSentPackets(OutboundRtpTrafficDTO outboundRtpTrafficDTO, Integer packetsSent) {
        if (Objects.isNull(packetsSent) || packetsSent < 0) {
            return;
        }
        if (outboundRtpTrafficDTO.firstPacketsSent < 0 || packetsSent < outboundRtpTrafficDTO.lastPacketsSent) {
            // either it is started now, or reset happened.
            outboundRtpTrafficDTO.firstPacketsSent = outboundRtpTrafficDTO.lastPacketsSent = packetsSent;
        } else {
            outboundRtpTrafficDTO.lastPacketsSent = packetsSent;
        }
    }
}
