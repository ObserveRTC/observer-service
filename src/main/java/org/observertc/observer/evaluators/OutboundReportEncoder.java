package org.observertc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.codecs.Encoder;
import org.observertc.observer.codecs.OutboundReportsCodec;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.common.OutboundReport;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Prototype
public class OutboundReportEncoder {

    private static final Logger logger = LoggerFactory.getLogger(OutboundReportEncoder.class);

    private Subject<List<OutboundReport>> outboundReportSubject = PublishSubject.create();
    private Encoder encoder;

    @Inject
    OutboundReportsCodec outboundReportsCodec;

    @PostConstruct
    void setup() {
        this.encoder = outboundReportsCodec.getEncoder();
    }

    public OutboundReportEncoder(ObserverConfig config) {

    }

    public Observable<List<OutboundReport>> getObservableOutboundReports() {
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

    public void encodeSfuEventReport(List<SfuEventReport> sfuEventReports) {
        if (Objects.isNull(sfuEventReports) || sfuEventReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeSfuEventReport, sfuEventReports);
    }

    public void encodeSfuMetaReport(List<SfuMetaReport> sfuMetaReports) {
        if (Objects.isNull(sfuMetaReports) || sfuMetaReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeSfuMetaReport, sfuMetaReports);
    }

    public void encodeSfuTransportReport(List<SFUTransportReport> sfuTransportReports) {
        if (Objects.isNull(sfuTransportReports) || sfuTransportReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeSfuTransportReport, sfuTransportReports);
    }

    public void encodeSfuInboundRtpPadReport(List<SfuInboundRtpPadReport> sfuRTPSourceReports) {
        if (Objects.isNull(sfuRTPSourceReports) || sfuRTPSourceReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeSfuInboundRtpPadReport, sfuRTPSourceReports);
    }

    public void encodeSfuOutboundRtpPadReport(List<SfuOutboundRtpPadReport> sfuRTPSinkReports) {
        if (Objects.isNull(sfuRTPSinkReports) || sfuRTPSinkReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeSfuOutboundRtpPadReport, sfuRTPSinkReports);
    }

    public void encodeSfuSctpStreamReport(List<SfuSctpStreamReport> sfuSctpStreamReports) {
        if (Objects.isNull(sfuSctpStreamReports) || sfuSctpStreamReports.size() < 1) {
            return;
        }
        this.convertAndForward(this.encoder::encodeSfuSctpStreamReport, sfuSctpStreamReports);
    }


    private<T> void convertAndForward(Function<T, OutboundReport> converter, List<T> reports) {
        List<OutboundReport> outboundReports = new LinkedList<>();
        for (T report : reports) {
            OutboundReport outboundReport;
            try {
                outboundReport = converter.apply(report);
            } catch (Exception ex) {
                logger.warn("Converting report {} is failed", JsonUtils.objectToString(report), ex);
                continue;
            }
            if (Objects.isNull(report)) {
                logger.warn("Converted report is null");
                continue;
            }
            outboundReports.add(outboundReport);
        }
        synchronized (this) {
            this.outboundReportSubject.onNext(outboundReports);
        }
    }
}
