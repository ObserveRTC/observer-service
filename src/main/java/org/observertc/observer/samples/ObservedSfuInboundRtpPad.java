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

    ObservedSfuStream getObservedSfuStream();
    Long getMinTimestamp();
    Long getMaxTimestamp();
    String getMarker();

    class Builder implements ObservedSfuInboundRtpPad {
        final ObservedSfuStream.Builder observedSfuStream;
        private final String padId;
        private List<Samples.SfuSample.SfuInboundRtpPad> sfuInboundRtpPadSamples = new LinkedList<>();

        public Builder(ObservedSfuStream.Builder observedSfuStream, String padId) {
            this.observedSfuStream = observedSfuStream;
            this.padId = padId;

            ObservedSfuSamples.Builder root = this.observedSfuStream.observedSfuTransport.observedSfu.observedSfuSamples;
            root.inboundRtpPadIds.add(padId);
        }

        public void add(Samples.SfuSample.SfuInboundRtpPad sfuInboundRtpPad) {
            this.sfuInboundRtpPadSamples.add(sfuInboundRtpPad);
        }

        @Override
        public String getSfuId() {
            return observedSfuStream.getSfuId();
        }

        @Override
        public String getSfuTransportId() {
            return observedSfuStream.getSfuTransportId();
        }

        @Override
        public String getSfuStreamId() {
            return observedSfuStream.getSfuStreamId();
        }

        @Override
        public String getPadId() {
            return padId;
        }

        @Override
        public ObservedSfuStream getObservedSfuStream() {
            return observedSfuStream;
        }

        @Override
        public Long getMinTimestamp() {
            return observedSfuStream.getMinTimestamp();
        }

        @Override
        public Long getMaxTimestamp() {
            return observedSfuStream.getMaxTimestamp();
        }

        @Override
        public String getMarker() {
            return observedSfuStream.getMarker();
        }

        @Override
        public Iterator<Samples.SfuSample.SfuInboundRtpPad> iterator() {
            return sfuInboundRtpPadSamples.iterator();
        }
    }
}
