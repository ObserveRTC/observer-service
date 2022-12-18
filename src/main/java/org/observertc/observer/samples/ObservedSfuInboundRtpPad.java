package org.observertc.observer.samples;

import org.observertc.schemas.samples.Samples;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public interface ObservedSfuInboundRtpPad extends Iterable<Samples.SfuSample.SfuInboundRtpPad> {

    String getSfuId();
    String getSfuTransportId();
    String getSfuStreamId();
    String getPadId();

    ObservedSfuTransport getObservedSfuTransport();
    Long getMinTimestamp();
    Long getMaxTimestamp();
    String getMarker();

    Long getSsrc();

    class Builder implements ObservedSfuInboundRtpPad {
        final ObservedSfuTransport.Builder observedSfuTransport;
        private final String sfuStreamId;
        private final String padId;
        private final Long ssrc;

        private List<Samples.SfuSample.SfuInboundRtpPad> sfuInboundRtpPadSamples = new LinkedList<>();

        public Builder(ObservedSfuTransport.Builder observedSfuTransport, String sfuStreamId, String padId, Long SSRC) {
            this.observedSfuTransport = observedSfuTransport;
            this.sfuStreamId = sfuStreamId;
            this.padId = padId;
            this.ssrc = SSRC;

            ObservedSfuSamples.Builder root = this.observedSfuTransport.observedSfu.observedSfuSamples;
            root.inboundRtpPadIds.add(padId);
        }

        public void add(Samples.SfuSample.SfuInboundRtpPad sfuInboundRtpPad) {
            this.sfuInboundRtpPadSamples.add(sfuInboundRtpPad);
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
            return ssrc;
        }

        @Override
        public Iterator<Samples.SfuSample.SfuInboundRtpPad> iterator() {
            return sfuInboundRtpPadSamples.iterator();
        }
    }
}
