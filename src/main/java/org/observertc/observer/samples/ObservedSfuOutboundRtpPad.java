package org.observertc.observer.samples;

import org.observertc.schemas.samples.Samples;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public interface ObservedSfuOutboundRtpPad extends Iterable<Samples.SfuSample.SfuOutboundRtpPad> {

    String getSfuId();
    String getSfuTransportId();
    String getSfuStreamId();
    String getSfuSinkId();
    String getPadId();

    ObservedSfuTransport getObservedSfuTransport();
    Long getMinTimestamp();
    Long getMaxTimestamp();
    String getMarker();

    Long getSsrc();

    class Builder implements ObservedSfuOutboundRtpPad {
        private final String sfuStreamId;
        private final String sfuSinkId;
        private final String padId;
        private final Long SSRC;
        final ObservedSfuTransport.Builder observedSfuTransport;
        private List<Samples.SfuSample.SfuOutboundRtpPad> outboundRtpPads = new LinkedList<>();

        public Builder(ObservedSfuTransport.Builder observedSfuTransport, String sfuStreamId, String sfuSinkId, String padId, Long SSRC) {
            this.observedSfuTransport = observedSfuTransport;
            this.sfuStreamId = sfuStreamId;
            this.sfuSinkId = sfuSinkId;
            this.padId = padId;
            this.SSRC = SSRC;

            ObservedSfuSamples.Builder root = this.observedSfuTransport.observedSfu.observedSfuSamples;
            root.outboundRtpPadIds.add(padId);
        }

        public void add(Samples.SfuSample.SfuOutboundRtpPad sfuOutboundRtpPad) {
            this.outboundRtpPads.add(sfuOutboundRtpPad);
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
        public String getPadId() {
            return padId;
        }

        @Override
        public ObservedSfuTransport getObservedSfuTransport() {
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
        public Long getSsrc() {
            return SSRC;
        }

        @Override
        public Iterator<Samples.SfuSample.SfuOutboundRtpPad> iterator() {
            return outboundRtpPads.iterator();
        }


    }
}
