package org.observertc.observer.samples;

import org.observertc.observer.common.FlatIterator;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public interface ObservedSfuSink extends Iterable<Samples.SfuSample.SfuOutboundRtpPad> {

    String getSfuId();
    String getSfuTransportId();
    String getSfuStreamId();
    String getSfuSinkId();

    ObservedSfuTransport getObservedTransport();
    Long getMinTimestamp();
    Long getMaxTimestamp();
    String getMarker();

    class Builder implements ObservedSfuSink {

        private static final Logger logger = LoggerFactory.getLogger(ObservedSfuSink.Builder.class);

        private String sfuSinkId = null;
        private String sfuStreamId = null;
        final ObservedSfuTransport.Builder observedSfuTransport;
        private Map<String, ObservedSfuOutboundRtpPad> observedSfuOutboundRtpPads = new HashMap<>();

        public Builder(ObservedSfuTransport.Builder observedSfuTransport, String sfuStreamId, String sfuSinkId) {
            this.observedSfuTransport = observedSfuTransport;
            this.sfuStreamId = sfuStreamId;
            this.sfuSinkId = sfuSinkId;

            ObservedSfuSamples.Builder root = this.observedSfuTransport.observedSfu.observedSfuSamples;
            root.sinkIds.add(sfuSinkId);
        }

        public void addOutboundRtpPad(Samples.SfuSample.SfuOutboundRtpPad sfuOutboundRtpPad) {
            if (sfuOutboundRtpPad.padId == null) {
                logger.warn("No padId for sfu stream {}, on transport {} at sfu {} mediaUnitId {}",
                        this.sfuSinkId,
                        this.observedSfuTransport.getSfuTransportId(),
                        this.observedSfuTransport.getSfuId(),
                        this.observedSfuTransport.getMediaUnitId()
                );
                return;
            }
            ObservedSfuOutboundRtpPad.Builder observedSfuOutboundRtpPad = (ObservedSfuOutboundRtpPad.Builder) observedSfuOutboundRtpPads.get(sfuOutboundRtpPad.padId);
            if (observedSfuOutboundRtpPad == null) {
                observedSfuOutboundRtpPad = new ObservedSfuOutboundRtpPad.Builder(this, sfuOutboundRtpPad.padId);
                observedSfuOutboundRtpPads.put(sfuOutboundRtpPad.padId, observedSfuOutboundRtpPad);
            }
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
        public String getSfuSinkId() {
            return sfuSinkId;
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

        @Override
        public Iterator<Samples.SfuSample.SfuOutboundRtpPad> iterator() {
            return new FlatIterator<Samples.SfuSample.SfuOutboundRtpPad>(
                    observedSfuOutboundRtpPads.values().iterator(),
                    observedSfuOutboundRtpPad -> observedSfuOutboundRtpPad.iterator()
            );
        }
    }
}
