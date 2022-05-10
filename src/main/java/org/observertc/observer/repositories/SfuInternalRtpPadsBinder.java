package org.observertc.observer.repositories;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Singleton
public class SfuInternalRtpPadsBinder {

    private static final Logger logger = LoggerFactory.getLogger(SfuInternalRtpPadsBinder.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {

    }

    @PreDestroy
    void teardown() {

    }

    public void onSfuRtpPadsAdded(List<SfuRtpPadDTO> sfuRtpPads) {
        sfuRtpPads.forEach(sfuRtpPad -> {
            if (Boolean.TRUE.equals(sfuRtpPad.internal) == false) {
                return;
            }
            switch (sfuRtpPad.streamDirection) {
                case INBOUND:
                    this.bindInternalSfuInboundRtpPad(sfuRtpPad);
                    return;
                case OUTBOUND:
                    this.bindInternalSfuOutboundRtpPad(sfuRtpPad);
                    return;
            }
        });
    }

    private void bindInternalSfuInboundRtpPad(SfuRtpPadDTO inboundSfuRtpPad) {
    }



    private void bindInternalSfuOutboundRtpPad(SfuRtpPadDTO outboundSfuRtpPad) {
        if (outboundSfuRtpPad.streamId == null) {
            return;
        }
        this.hazelcastMaps.getSfuStreamIdToInternalOutboundRtpPadIds().put(outboundSfuRtpPad.streamId, outboundSfuRtpPad.rtpPadId);
    }
}
