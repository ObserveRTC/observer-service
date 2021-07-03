package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.OutboundReports;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.codecs.OutboundReportsAvroEncoder;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class OutboundReportEncoder extends Observable<OutboundReport> {

    private static final Logger logger = LoggerFactory.getLogger(OutboundReportEncoder.class);

    private Observer<? super OutboundReport> observer;

    @Inject
    OutboundReportsAvroEncoder encoder;

    @PostConstruct
    void setup() {

    }

    public OutboundReportEncoder(ObserverConfig config) {

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
        callEventReports.forEach(callEventReport -> {
            logger.info("Call Event Report {} is being encoded", callEventReport.getName());
        });
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
        List<OutboundReport> outboundReports = new LinkedList<>();
        for (T report : reports) {
            OutboundReport outboundReport;
            try {
                outboundReport = converter.apply(report);
            } catch (Exception ex) {
                logger.warn("Converting report {} is failed", ObjectToString.toString(report), ex);
                continue;
            }
            if (Objects.isNull(report)) {
                logger.warn("Converted report is null");
                continue;
            }
            outboundReports.add(outboundReport);
        }
        synchronized (this) {
            outboundReports.stream().forEach(this::forward);
        }
    }
}
