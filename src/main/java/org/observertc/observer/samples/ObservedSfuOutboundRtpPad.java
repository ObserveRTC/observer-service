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

    ObservedSfuSink getObservedSfuSink();
    Long getMinTimestamp();
    Long getMaxTimestamp();
    String getMarker();

    class Builder implements ObservedSfuOutboundRtpPad {
        private String sfuStreamId = null;
        private String sfuSinkId = null;
        private final String padId;
        private final ObservedSfuSink.Builder observedSfuSink;
        private List<Samples.SfuSample.SfuOutboundRtpPad> outboundRtpPads = new LinkedList<>();

        public Builder(ObservedSfuSink.Builder observedSfuSink, String padId) {
            this.observedSfuSink = observedSfuSink;
            this.padId = padId;

            ObservedSfuSamples.Builder root = this.observedSfuSink.observedSfuTransport.observedSfu.observedSfuSamples;
            root.outboundRtpPadIds.add(padId);
        }

        public void add(Samples.SfuSample.SfuOutboundRtpPad sfuOutboundRtpPad) {
            this.outboundRtpPads.add(sfuOutboundRtpPad);
        }

        @Override
        public String getSfuId() {
            return observedSfuSink.getSfuId();
        }

        @Override
        public String getSfuTransportId() {
            return observedSfuSink.getSfuTransportId();
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
        public ObservedSfuSink getObservedSfuSink() {
            return observedSfuSink;
        }

        @Override
        public Long getMinTimestamp() {
            return observedSfuSink.getMinTimestamp();
        }

        @Override
        public Long getMaxTimestamp() {
            return observedSfuSink.getMaxTimestamp();
        }

        @Override
        public String getMarker() {
            return observedSfuSink.getMarker();
        }

        @Override
        public Iterator<Samples.SfuSample.SfuOutboundRtpPad> iterator() {
            return outboundRtpPads.iterator();
        }


    }
}
