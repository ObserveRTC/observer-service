package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.MessageEncoder;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.codecs.OutboundReportsAvroEncoder;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class OutboundReportEncoder extends Observable<OutboundReport> {

    private static final Logger logger = LoggerFactory.getLogger(OutboundReportEncoder.class);

    private Observer<? super OutboundReport> observer;
    private final BinaryMessageEncoder<ObserverEventReport> observerEventEncoder;
    private final BinaryMessageEncoder<CallEventReport> callEventEncoder;
    private final BinaryMessageEncoder<CallMetaReport> callMetaEncoder;
    private final BinaryMessageEncoder<ClientExtensionReport> clientExtensionEncoder;
    private final BinaryMessageEncoder<PcTransportReport> pcTransportEncoder;
    private final BinaryMessageEncoder<PcDataChannelReport> pcDataChannelEncoder;
    private final BinaryMessageEncoder<InboundAudioTrackReport> inboundAudioTrackEncoder;
    private final BinaryMessageEncoder<InboundVideoTrackReport> inboundVideoTrackEncoder;
    private final BinaryMessageEncoder<OutboundAudioTrackReport> outboundAudioTrackEncoder;
    private final BinaryMessageEncoder<OutboundVideoTrackReport> outboundVideoTrackEncoder;
    private final BinaryMessageEncoder<MediaTrackReport> mediaTrackEncoder;

    public OutboundReportEncoder(ObserverConfig config) {
        this.observerEventEncoder = new BinaryMessageEncoder<ObserverEventReport>(GenericData.get(), ObserverEventReport.getClassSchema());
        this.callEventEncoder = new BinaryMessageEncoder<CallEventReport>(GenericData.get(), CallEventReport.getClassSchema());
        this.callMetaEncoder = new BinaryMessageEncoder<CallMetaReport>(GenericData.get(), CallMetaReport.getClassSchema());
        this.clientExtensionEncoder = new BinaryMessageEncoder<ClientExtensionReport>(GenericData.get(), ClientExtensionReport.getClassSchema());
        this.pcTransportEncoder = new BinaryMessageEncoder<PcTransportReport>(GenericData.get(), PcTransportReport.getClassSchema());
        this.pcDataChannelEncoder = new BinaryMessageEncoder<PcDataChannelReport>(GenericData.get(), PcDataChannelReport.getClassSchema());
        this.inboundAudioTrackEncoder = new BinaryMessageEncoder<InboundAudioTrackReport>(GenericData.get(), InboundAudioTrackReport.getClassSchema());
        this.inboundVideoTrackEncoder = new BinaryMessageEncoder<InboundVideoTrackReport>(GenericData.get(), InboundVideoTrackReport.getClassSchema());
        this.outboundAudioTrackEncoder = new BinaryMessageEncoder<OutboundAudioTrackReport>(GenericData.get(), OutboundAudioTrackReport.getClassSchema());
        this.outboundVideoTrackEncoder = new BinaryMessageEncoder<OutboundVideoTrackReport>(GenericData.get(), OutboundVideoTrackReport.getClassSchema());
        this.mediaTrackEncoder = new BinaryMessageEncoder<MediaTrackReport>(GenericData.get(), MediaTrackReport.getClassSchema());
    }

    @Inject
    OutboundReportsAvroEncoder encoder;

    @PostConstruct
    void setup() {

    }

    public void encodeObserverEventReports(List<ObserverEventReport> observerEventReports) {
        if (Objects.isNull(observerEventReports) || observerEventReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeObserverEventReport, observerEventReports);
    }

    public void encodeCallEventReports(List<CallEventReport> callEventReports) {
        if (Objects.isNull(callEventReports) || callEventReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeCallEventReport, callEventReports);
    }

    public void encodeClientExtensionReport(List<ClientExtensionReport> clientExtensionReports) {
        if (Objects.isNull(clientExtensionReports) || clientExtensionReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeClientExtensionReport, clientExtensionReports);
    }

    public void encodeCallMetaReports(List<CallMetaReport> callMetaReports) {
        if (Objects.isNull(callMetaReports) || callMetaReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeCallMetaReport, callMetaReports);
    }

    public void encodePcTransportReport(List<PcTransportReport> pcTransportReports) {
        if (Objects.isNull(pcTransportReports) || pcTransportReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodePcTransportReport, pcTransportReports);
    }

    public void encodePcDataChannelReport(List<PcDataChannelReport> pcDataChannelReports) {
        if (Objects.isNull(pcDataChannelReports) || pcDataChannelReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodePcDataChannelReport, pcDataChannelReports);
    }

    public void encodeInboundAudioTrackReport(List<InboundAudioTrackReport> inboundAudioTrackReports) {
        if (Objects.isNull(inboundAudioTrackReports) || inboundAudioTrackReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeInboundAudioTrackReport, inboundAudioTrackReports);
    }

    public void encodeInboundVideoTrackReport(List<InboundVideoTrackReport> inboundVideoTrackReports) {
        if (Objects.isNull(inboundVideoTrackReports) || inboundVideoTrackReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeInboundVideoTrackReport, inboundVideoTrackReports);
    }

    public void encodeOutboundAudioTrackReport(List<OutboundAudioTrackReport> outboundAudioTrackReports) {
        if (Objects.isNull(outboundAudioTrackReports) || outboundAudioTrackReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeOutboundAudioTrackReport, outboundAudioTrackReports);
    }

    public void encodeOutboundVideoTrackReport(List<OutboundVideoTrackReport> outboundVideoTrackReports) {
        if (Objects.isNull(outboundVideoTrackReports) || outboundVideoTrackReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeOutboundVideoTrackReport, outboundVideoTrackReports);
    }

    public void encodeMediaTrackReport(List<MediaTrackReport> mediaTrackReports) {
        if (Objects.isNull(mediaTrackReports) || mediaTrackReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeMediaTrackReport, mediaTrackReports);
    }

    @Override
    protected void subscribeActual(@NonNull Observer<? super OutboundReport> observer) {
        if (Objects.nonNull(this.observer)) {
            throw new RuntimeException(this.getClass().getSimpleName() + " cannot connected to more than one observer");
        }
        this.observer = observer;
    }

    private void forward(OutboundReport outboundReport) {
        Objects.requireNonNull(this.observer);
        this.observer.onNext(outboundReport);
    }

    private<T> void convertAndForward(Function<T, OutboundReport> converter, List<T> reports) {
        List<OutboundReport> outboundReports = reports
                .stream()
                .map(converter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        synchronized (this) {
            outboundReports.stream().forEach(this::forward);
        }
    }
}
