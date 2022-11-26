package org.observertc.observer.samples;

import org.jetbrains.annotations.NotNull;
import org.observertc.observer.common.FlatIterator;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public interface ObservedSfuStream extends Iterable<Samples.SfuSample.SfuInboundRtpPad> {

    String getSfuId();
    String getSfuTransportId();
    String getSfuStreamId();

    ObservedSfuTransport getObservedTransport();
    Long getMinTimestamp();
    Long getMaxTimestamp();
    String getMarker();

    class Builder implements ObservedSfuStream {

        private static final Logger logger = LoggerFactory.getLogger(ObservedSfuStream.Builder.class);

        private String sfuStreamId = null;
        final ObservedSfuTransport.Builder observedSfuTransport;
        private Map<String, ObservedSfuInboundRtpPad> observedSfuInboundRtpPads = new HashMap<>();

        public Builder(ObservedSfuTransport.Builder observedSfuTransport, String sfuStreamId) {
            this.observedSfuTransport = observedSfuTransport;
            this.sfuStreamId = sfuStreamId;

            ObservedSfuSamples.Builder root = this.observedSfuTransport.observedSfu.observedSfuSamples;
            root.streamIds.add(sfuStreamId);
        }

        public void addInboundRtpPad(Samples.SfuSample.SfuInboundRtpPad sfuInboundRtpPad) {
            if (sfuInboundRtpPad.padId == null) {
                logger.warn("No padId for sfu stream {}, on transport {} at sfu {} mediaUnitId {}",
                        this.sfuStreamId,
                        this.observedSfuTransport.getSfuTransportId(),
                        this.observedSfuTransport.getSfuId(),
                        this.observedSfuTransport.getMediaUnitId()
                );
                return;
            }
            ObservedSfuInboundRtpPad.Builder observedSfuInboundRtpPad = (ObservedSfuInboundRtpPad.Builder) observedSfuInboundRtpPads.get(sfuInboundRtpPad.padId);
            if (observedSfuInboundRtpPad == null) {
                observedSfuInboundRtpPad = new ObservedSfuInboundRtpPad.Builder(this, sfuInboundRtpPad.padId);
                observedSfuInboundRtpPads.put(sfuInboundRtpPad.padId, observedSfuInboundRtpPad);
            }
            observedSfuInboundRtpPad.add(sfuInboundRtpPad);
        }

        @Override
        public String getSfuId() {
            return observedSfuTransport.getSfuId();
        }

        @Override
        public String getSfuTransportId() {
            return observedSfuTransport.getSfuTransportId();
        }

        @Override
        public String getSfuStreamId() {
            return sfuStreamId;
        }

        @Override
        public ObservedSfuTransport getObservedTransport() {
            return observedSfuTransport;
        }

        @Override
        public Long getMinTimestamp() {
            return observedSfuTransport.getMinTimestamp();
        }

        @Override
        public Long getMaxTimestamp() {
            return observedSfuTransport.getMaxTimestamp();
        }

        @Override
        public String getMarker() {
            return observedSfuTransport.getMarker();
        }

        @NotNull
        @Override
        public Iterator<Samples.SfuSample.SfuInboundRtpPad> iterator() {
            return new FlatIterator<Samples.SfuSample.SfuInboundRtpPad>(
                    observedSfuInboundRtpPads.values().iterator(),
                    observedSfuInboundRtpPad -> observedSfuInboundRtpPad.iterator()
            );
        }
    }
}
