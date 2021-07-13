package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.codecs.OutboundReportsAvroEncoder;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.configs.ObserverConfig;
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

@Singleton
public class OutboundReportEncoder {

    private static final Logger logger = LoggerFactory.getLogger(OutboundReportEncoder.class);

    private Subject<OutboundReport> outboundReportSubject = PublishSubject.create();

    @Inject
    OutboundReportsAvroEncoder encoder;

    @PostConstruct
    void setup() {

    }

    public OutboundReportEncoder(ObserverConfig config) {

    }

    public Observable<OutboundReport> getObservableOutboundReport() {
        return this.outboundReportSubject;
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

    public void encodeClientTransportReport(List<ClientTransportReport> clientTransportReports) {
        if (Objects.isNull(clientTransportReports) || clientTransportReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeClientTransportReport, clientTransportReports);
    }

    public void encodeClientDataChannelReport(List<ClientDataChannelReport> clientDataChannelReports) {
        if (Objects.isNull(clientDataChannelReports) || clientDataChannelReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeClientDataChannelReport, clientDataChannelReports);
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
            outboundReports.stream().forEach(this.outboundReportSubject::onNext);
        }
    }
}
