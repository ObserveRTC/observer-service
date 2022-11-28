package org.observertc.observer.samples;

import org.jetbrains.annotations.NotNull;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ObservedSfu extends Iterable<Samples.SfuSample> {

    String getSfuId();
    String getMarker();
    Long getMinTimestamp();
    Long getMaxTimestamp();

    String getServiceId();
    String getMediaUnitId();

    Stream<ObservedSfuSample> streamObservedSfuSamples();
    Iterable<ObservedSfuSample> observedSfuSamples();
    Iterable<ObservedSfuTransport> observedSfuTransports();

    String getTimeZoneId();

    class Builder implements ObservedSfu {

        private static final Logger logger = LoggerFactory.getLogger(ObservedSfu.class);

        final ObservedSfuSamples.Builder observedSfuSamples;

        private final String sfuId;
        private final String serviceId;
        private final String mediaUnitId;

        private String timeZoneId = null;
        private String marker = null;
        private Long minTimestamp = null;
        private Long maxTimestamp = null;
        private Map<String, ObservedSfuTransport> observedSfuTransports = new HashMap<>();
        private List<Samples.SfuSample> samples = new LinkedList<>();

        public Builder(ObservedSfuSamples.Builder observedSfuSamples, String sfuId, String serviceId, String mediaUnitId) {
            this.observedSfuSamples = observedSfuSamples;
            this.sfuId = sfuId;
            this.serviceId = serviceId;
            this.mediaUnitId = mediaUnitId;

            ObservedSfuSamples.Builder root = this.observedSfuSamples;
            root.sfuIds.add(sfuId);
        }

        public void add(Samples.SfuSample sfuSample) {
            SfuSampleVisitor.streamTransports(sfuSample).forEach(sfuTransportSample -> {
                if (sfuTransportSample.transportId == null) {
                    logger.warn("TransportId for sfu {} is null", sfuSample.sfuId);
                    return;
                }
                ObservedSfuTransport.Builder observedSfuTransport = (ObservedSfuTransport.Builder) this.observedSfuTransports.get(sfuTransportSample.transportId);
                if (observedSfuTransport == null) {
                    observedSfuTransport = new ObservedSfuTransport.Builder(this, sfuTransportSample.transportId, Boolean.TRUE.equals(sfuTransportSample.internal));
                    this.observedSfuTransports.put(observedSfuTransport.getSfuTransportId(), observedSfuTransport);
                }
            });

            SfuSampleVisitor.streamInboundRtpPads(sfuSample).forEach(sfuInboundRtpPad -> {
                if (sfuInboundRtpPad.transportId == null) {
                    logger.warn("TransportId for Inbound Rtp Pad for sfu {} is null", sfuSample.sfuId);
                    return;
                }
                ObservedSfuTransport.Builder observedSfuTransport = (ObservedSfuTransport.Builder) this.observedSfuTransports.get(sfuInboundRtpPad.transportId);
                if (observedSfuTransport == null) {
                    observedSfuTransport = new ObservedSfuTransport.Builder(this, sfuInboundRtpPad.transportId, Boolean.TRUE.equals(sfuInboundRtpPad.internal));
                    this.observedSfuTransports.put(observedSfuTransport.getSfuTransportId(), observedSfuTransport);
                }
                observedSfuTransport.addSfuInboundRtpPad(sfuInboundRtpPad);
            });

            SfuSampleVisitor.streamOutboundRtpPads(sfuSample).forEach(sfuOutboundRtpPad -> {
                if (sfuOutboundRtpPad.transportId == null) {
                    logger.warn("TransportId for Outbound Rtp Pad Sample for sfu {} is null", sfuSample.sfuId);
                    return;
                }
                ObservedSfuTransport.Builder observedSfuTransport = (ObservedSfuTransport.Builder) this.observedSfuTransports.get(sfuOutboundRtpPad.transportId);
                if (observedSfuTransport == null) {
                    observedSfuTransport = new ObservedSfuTransport.Builder(this, sfuOutboundRtpPad.transportId, Boolean.TRUE.equals(sfuOutboundRtpPad.internal));
                    this.observedSfuTransports.put(observedSfuTransport.getSfuTransportId(), observedSfuTransport);
                }
                observedSfuTransport.addSfuOutboundRtpPad(sfuOutboundRtpPad);
            });

            SfuSampleVisitor.streamSctpStreams(sfuSample).forEach(sfuSctpChannel -> {
                if (sfuSctpChannel.transportId == null) {
                    logger.warn("TransportId in SfuSctpChannel Sample for sfu {} is null", sfuSample.sfuId);
                    return;
                }
                ObservedSfuTransport.Builder observedSfuTransport = (ObservedSfuTransport.Builder) this.observedSfuTransports.get(sfuSctpChannel.transportId);
                if (observedSfuTransport == null) {
                    observedSfuTransport = new ObservedSfuTransport.Builder(this, sfuSctpChannel.transportId, Boolean.TRUE.equals(sfuSctpChannel.internal));
                    this.observedSfuTransports.put(observedSfuTransport.getSfuTransportId(), observedSfuTransport);
                }
                observedSfuTransport.addSfuSctpChannel(sfuSctpChannel);
            });

            if (this.minTimestamp == null || sfuSample.timestamp < this.minTimestamp) {
                this.minTimestamp = sfuSample.timestamp;
            }
            if (this.maxTimestamp == null || this.maxTimestamp < sfuSample.timestamp) {
                this.maxTimestamp = sfuSample.timestamp;
            }
            if (this.marker == null) {
                this.marker = sfuSample.marker;
            }
            if (this.timeZoneId == null && sfuSample.timeZoneOffsetInHours != null) {
                this.timeZoneId = this.observedSfuSamples.minuteToTimeZoneOffsetConverter.apply(sfuSample.timeZoneOffsetInHours);
            }
            this.samples.add(sfuSample);
        }

        @Override
        public String getSfuId() {
            return this.sfuId;
        }

        @Override
        public Long getMinTimestamp() {
            return minTimestamp;
        }

        @Override
        public Long getMaxTimestamp() {
            return maxTimestamp;
        }

        @Override
        public String getServiceId() {
            return serviceId;
        }

        @Override
        public String getMediaUnitId() {
            return mediaUnitId;
        }

        @Override
        public Stream<ObservedSfuSample> streamObservedSfuSamples() {
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(this.observedSfuSamples().iterator(), Spliterator.ORDERED),
                    false);
        }

        @Override
        public Iterable<ObservedSfuSample> observedSfuSamples() {
            return () -> {
                var it = samples.iterator();
                return new Iterator<ObservedSfuSample>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public ObservedSfuSample next() {
                        var sfuSample = it.next();
                        return new ObservedSfuSample() {
                            @Override
                            public String getMediaUnitId() {
                                return mediaUnitId;
                            }

                            @Override
                            public String getTimeZoneId() {
                                return timeZoneId;
                            }

                            @Override
                            public String getServiceId() {
                                return serviceId;
                            }

                            @Override
                            public Samples.SfuSample getSfuSample() {
                                return sfuSample;
                            }
                        };
                    }
                };
            };
        }

        @Override
        public Iterable<ObservedSfuTransport> observedSfuTransports() {
            return () -> this.observedSfuTransports.values().iterator();
        }

        @Override
        public String getTimeZoneId() {
            return timeZoneId;
        }

        @Override
        public String getMarker() {
            return this.marker;
        }

        @NotNull
        @Override
        public Iterator<Samples.SfuSample> iterator() {
            return this.samples.iterator();
        }
    }
}
