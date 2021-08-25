package org.observertc.webrtc.observer.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface OutboundReportTypeVisitor<TIn, TOut> extends BiFunction<TIn, ReportType, TOut> {
    static Logger logger = LoggerFactory.getLogger(OutboundReportTypeVisitor.class);

    static <RIn, ROut> OutboundReportTypeVisitor<RIn, ROut> createFunctionalVisitor(
            Function<RIn, ROut> observerEventFuncProcess,
            Function<RIn, ROut> callEventFuncProcess,
            Function<RIn, ROut> callMetaDataFuncProcess,
            Function<RIn, ROut> clientExtensionDataFuncProcess,
            Function<RIn, ROut> clientTransportFuncProcess,
            Function<RIn, ROut> clientDataChannelFuncProcess,
            Function<RIn, ROut> inboundAudioTrackFuncProcess,
            Function<RIn, ROut> inboundVideoTrackFuncProcess,
            Function<RIn, ROut> outboundAudioTrackFuncProcess,
            Function<RIn, ROut> outboundVideoTrackFuncProcess,
            Function<RIn, ROut> mediaTrackFuncProcess,
            Function<RIn, ROut> sfuEventFuncProcess,
            Function<RIn, ROut> sfuMetaFuncProcess,
            Function<RIn, ROut> sfuTransportFuncProcess,
            Function<RIn, ROut> sfuRtpSourceFuncProcess,
            Function<RIn, ROut> sfuRtpSinkFuncProcess,
            Function<RIn, ROut> sfuSctpStreamFuncProcess
    ) {
        return new OutboundReportTypeVisitor<RIn, ROut>() {
            @Override
            public ROut visitObserverEventReport(RIn obj) {
                return observerEventFuncProcess.apply(obj);
            }

            @Override
            public ROut visitCallEventReport(RIn obj) {
                return callEventFuncProcess.apply(obj);
            }

            @Override
            public ROut visitCallMetaDataReport(RIn obj) {
                return callMetaDataFuncProcess.apply(obj);
            }

            @Override
            public ROut visitClientExtensionDataReport(RIn obj) {
                return clientExtensionDataFuncProcess.apply(obj);
            }

            @Override
            public ROut visitClientTransportReport(RIn obj) {
                return clientTransportFuncProcess.apply(obj);
            }

            @Override
            public ROut visitClientDataChannelReport(RIn obj) {
                return clientDataChannelFuncProcess.apply(obj);
            }

            @Override
            public ROut visitInboundAudioTrackReport(RIn obj) {
                return inboundAudioTrackFuncProcess.apply(obj);
            }

            @Override
            public ROut visitInboundVideoTrackReport(RIn obj) {
                return inboundVideoTrackFuncProcess.apply(obj);
            }

            @Override
            public ROut visitOutboundAudioTrackReport(RIn obj) {
                return outboundAudioTrackFuncProcess.apply(obj);
            }

            @Override
            public ROut visitOutboundVideoTrackReport(RIn obj) {
                return outboundVideoTrackFuncProcess.apply(obj);
            }

            @Override
            public ROut visitMediaTrackReport(RIn obj) {
                return mediaTrackFuncProcess.apply(obj);
            }

            @Override
            public ROut visitSfuEventReport(RIn obj) {
                return sfuEventFuncProcess.apply(obj);
            }

            @Override
            public ROut visitSfuMetaReport(RIn obj) {
                return sfuMetaFuncProcess.apply(obj);
            }

            @Override
            public ROut visitSfuTransportReport(RIn obj) {
                return sfuTransportFuncProcess.apply(obj);
            }

            @Override
            public ROut visitSfuRtpSourceReport(RIn obj) {
                return sfuRtpSourceFuncProcess.apply(obj);
            }

            @Override
            public ROut visitSfuRtpSinkReport(RIn obj) {
                return sfuRtpSinkFuncProcess.apply(obj);
            }

            @Override
            public ROut visitSctpStreamReport(RIn obj) {
                return sfuSctpStreamFuncProcess.apply(obj);
            }
        };
    }


    static <RIn> OutboundReportTypeVisitor<RIn, Void> createConsumerVisitor(
            Consumer<RIn> observerEventConsumer,
            Consumer<RIn> callEventConsumer,
            Consumer<RIn> callMetaDataConsumer,
            Consumer<RIn> clientExtensionDataConsumer,
            Consumer<RIn> clientTransportConsumer,
            Consumer<RIn> clientDataChannelConsumer,
            Consumer<RIn> inboundAudioTrackConsumer,
            Consumer<RIn> inboundVideoTrackConsumer,
            Consumer<RIn> outboundAudioTrackConsumer,
            Consumer<RIn> outboundVideoTrackConsumer,
            Consumer<RIn> mediaTrackConsumer,
            Consumer<RIn> sfuEventConsumer,
            Consumer<RIn> sfuMetaConsumer,
            Consumer<RIn> sfuTransportConsumer,
            Consumer<RIn> sfuRtpSourceConsumer,
            Consumer<RIn> sfuRtpSinkConsumer,
            Consumer<RIn> sfuSctpStreamConsumer
    ) {
        return new OutboundReportTypeVisitor<RIn, Void>() {
            @Override
            public Void visitObserverEventReport(RIn obj) {
                observerEventConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitCallEventReport(RIn obj) {
                callEventConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitCallMetaDataReport(RIn obj) {
                callMetaDataConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitClientExtensionDataReport(RIn obj) {
                clientExtensionDataConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitClientTransportReport(RIn obj) {
                clientTransportConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitClientDataChannelReport(RIn obj) {
                clientDataChannelConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitInboundAudioTrackReport(RIn obj) {
                inboundAudioTrackConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitInboundVideoTrackReport(RIn obj) {
                inboundVideoTrackConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitOutboundAudioTrackReport(RIn obj) {
                outboundAudioTrackConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitOutboundVideoTrackReport(RIn obj) {
                outboundVideoTrackConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitMediaTrackReport(RIn obj) {
                mediaTrackConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitSfuEventReport(RIn obj) {
                sfuEventConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitSfuMetaReport(RIn obj) {
                sfuMetaConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitSfuTransportReport(RIn obj) {
                sfuTransportConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitSfuRtpSourceReport(RIn obj) {
                sfuRtpSourceConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitSfuRtpSinkReport(RIn obj) {
                sfuRtpSinkConsumer.accept(obj);
                return null;
            }

            @Override
            public Void visitSctpStreamReport(RIn obj) {
                sfuSctpStreamConsumer.accept(obj);
                return null;
            }
        };
    }

    static <ROut> OutboundReportTypeVisitor<Void, ROut> createSupplierVisitor(
            Supplier<ROut> observerEventSupplier,
            Supplier<ROut> callEventSupplier,
            Supplier<ROut> callMetaDataSupplier,
            Supplier<ROut> clientExtensionDataSupplier,
            Supplier<ROut> clientTransportSupplier,
            Supplier<ROut> clientDataChannelSupplier,
            Supplier<ROut> inboundAudioTrackSupplier,
            Supplier<ROut> inboundVideoTrackSupplier,
            Supplier<ROut> outboundAudioTrackSupplier,
            Supplier<ROut> outboundVideoTrackSupplier,
            Supplier<ROut> mediaTrackSupplier,
            Supplier<ROut> sfuEventSupplier,
            Supplier<ROut> sfuMetaSupplier,
            Supplier<ROut> sfuTransportSupplier,
            Supplier<ROut> sfuRtpSourceSupplier,
            Supplier<ROut> sfuRtpSinkSupplier,
            Supplier<ROut> sfuSctpStreamSupplier
    ) {
        return new OutboundReportTypeVisitor<Void, ROut>() {
            @Override
            public ROut visitObserverEventReport(Void obj) {
                return observerEventSupplier.get();

            }

            @Override
            public ROut visitCallEventReport(Void obj) {
                return callEventSupplier.get();
            }

            @Override
            public ROut visitCallMetaDataReport(Void obj) {
                return callMetaDataSupplier.get();
            }

            @Override
            public ROut visitClientExtensionDataReport(Void obj) {
                return clientExtensionDataSupplier.get();
            }

            @Override
            public ROut visitClientTransportReport(Void obj) {
                return clientTransportSupplier.get();
            }

            @Override
            public ROut visitClientDataChannelReport(Void obj) {
                return clientDataChannelSupplier.get();
            }

            @Override
            public ROut visitInboundAudioTrackReport(Void obj) {
                return inboundAudioTrackSupplier.get();
            }

            @Override
            public ROut visitInboundVideoTrackReport(Void obj) {
                return inboundVideoTrackSupplier.get();
            }

            @Override
            public ROut visitOutboundAudioTrackReport(Void obj) {
                return outboundAudioTrackSupplier.get();
            }

            @Override
            public ROut visitOutboundVideoTrackReport(Void obj) {
                return outboundVideoTrackSupplier.get();
            }

            @Override
            public ROut visitMediaTrackReport(Void obj) {
                return mediaTrackSupplier.get();
            }

            @Override
            public ROut visitSfuEventReport(Void obj) {
                return sfuEventSupplier.get();
            }

            @Override
            public ROut visitSfuMetaReport(Void obj) {
                return sfuMetaSupplier.get();
            }

            @Override
            public ROut visitSfuTransportReport(Void obj) {
                return sfuTransportSupplier.get();
            }

            @Override
            public ROut visitSfuRtpSourceReport(Void obj) {
                return sfuRtpSourceSupplier.get();
            }

            @Override
            public ROut visitSfuRtpSinkReport(Void obj) {
                return sfuRtpSinkSupplier.get();
            }

            @Override
            public ROut visitSctpStreamReport(Void obj) {
                return sfuSctpStreamSupplier.get();
            }
        };
    }

    static OutboundReportTypeVisitor<Void, Void> createRunnableVisitor(
            Runnable observerEventCallback,
            Runnable callEventCallback,
            Runnable callMetaDataCallback,
            Runnable clientExtensionDataCallback,
            Runnable clientTransportCallback,
            Runnable clientDataChannelCallback,
            Runnable inboundAudioTrackCallback,
            Runnable inboundVideoTrackCallback,
            Runnable outboundAudioTrackCallback,
            Runnable outboundVideoTrackCallback,
            Runnable mediaTrackCallback,
            Runnable sfuEventCallback,
            Runnable sfuMetaCallback,
            Runnable sfuTransportCallback,
            Runnable sfuRtpSourceCallback,
            Runnable sfuRtpSinkCallback,
            Runnable sfuSctpStreamCallback
    ) {
        return new OutboundReportTypeVisitor<Void, Void>() {
            @Override
            public Void visitObserverEventReport(Void obj) {
                observerEventCallback.run();
                return null;
            }

            @Override
            public Void visitCallEventReport(Void obj) {
                callEventCallback.run();
                return null;
            }

            @Override
            public Void visitCallMetaDataReport(Void obj) {
                callMetaDataCallback.run();
                return null;
            }

            @Override
            public Void visitClientExtensionDataReport(Void obj) {
                clientExtensionDataCallback.run();
                return null;
            }

            @Override
            public Void visitClientTransportReport(Void obj) {
                clientTransportCallback.run();
                return null;
            }

            @Override
            public Void visitClientDataChannelReport(Void obj) {
                clientDataChannelCallback.run();
                return null;
            }

            @Override
            public Void visitInboundAudioTrackReport(Void obj) {
                inboundAudioTrackCallback.run();
                return null;
            }

            @Override
            public Void visitInboundVideoTrackReport(Void obj) {
                inboundVideoTrackCallback.run();
                return null;
            }

            @Override
            public Void visitOutboundAudioTrackReport(Void obj) {
                outboundAudioTrackCallback.run();
                return null;
            }

            @Override
            public Void visitOutboundVideoTrackReport(Void obj) {
                outboundVideoTrackCallback.run();
                return null;
            }

            @Override
            public Void visitMediaTrackReport(Void obj) {
                mediaTrackCallback.run();
                return null;
            }

            @Override
            public Void visitSfuEventReport(Void obj) {
                sfuEventCallback.run();
                return null;
            }

            @Override
            public Void visitSfuMetaReport(Void obj) {
                sfuMetaCallback.run();
                return null;
            }

            @Override
            public Void visitSfuTransportReport(Void obj) {
                sfuTransportCallback.run();
                return null;
            }

            @Override
            public Void visitSfuRtpSourceReport(Void obj) {
                sfuRtpSourceCallback.run();
                return null;
            }

            @Override
            public Void visitSfuRtpSinkReport(Void obj) {
                sfuRtpSinkCallback.run();
                return null;
            }

            @Override
            public Void visitSctpStreamReport(Void obj) {
                sfuSctpStreamCallback.run();
                return null;
            }
        };
    }

    @Override
    default TOut apply(TIn obj, ReportType reportType) {
        switch (reportType) {
            case OBSERVER_EVENT:
                return this.visitObserverEventReport(obj);
            case CALL_EVENT:
                return this.visitCallEventReport(obj);
            case CALL_META_DATA:
                return this.visitCallMetaDataReport(obj);
            case CLIENT_EXTENSION_DATA:
                return this.visitClientExtensionDataReport(obj);
            case PEER_CONNECTION_TRANSPORT:
                return this.visitClientTransportReport(obj);
            case PEER_CONNECTION_DATA_CHANNEL:
                return this.visitClientDataChannelReport(obj);
            case INBOUND_AUDIO_TRACK:
                return this.visitInboundAudioTrackReport(obj);
            case INBOUND_VIDEO_TRACK:
                return this.visitInboundVideoTrackReport(obj);
            case OUTBOUND_AUDIO_TRACK:
                return this.visitOutboundAudioTrackReport(obj);
            case OUTBOUND_VIDEO_TRACK:
                return this.visitOutboundVideoTrackReport(obj);
            case MEDIA_TRACK:
                return this.visitMediaTrackReport(obj);

            case SFU_EVENT:
                return this.visitSfuEventReport(obj);
            case SFU_META_DATA:
                return this.visitSfuMetaReport(obj);
            case SFU_TRANSPORT:
                return this.visitSfuTransportReport(obj);
            case SFU_RTP_SOURCE_STREAM:
                return this.visitSfuRtpSourceReport(obj);
            case SFU_RTP_SINK_STREAM:
                return this.visitSfuRtpSinkReport(obj);
            case SFU_SCTP_STREAM:
                return this.visitSctpStreamReport(obj);
            default:
                logger.warn("Unrecognized report type {}", reportType);
        }
        return null;
    }

    TOut visitObserverEventReport(TIn obj);
    TOut visitCallEventReport(TIn obj);
    TOut visitCallMetaDataReport(TIn obj);
    TOut visitClientExtensionDataReport(TIn obj);
    TOut visitClientTransportReport(TIn obj);
    TOut visitClientDataChannelReport(TIn obj);
    TOut visitInboundAudioTrackReport(TIn obj);
    TOut visitInboundVideoTrackReport(TIn obj);
    TOut visitOutboundAudioTrackReport(TIn obj);
    TOut visitOutboundVideoTrackReport(TIn obj);
    TOut visitMediaTrackReport(TIn obj);

    TOut visitSfuEventReport(TIn obj);
    TOut visitSfuMetaReport(TIn obj);
    TOut visitSfuTransportReport(TIn obj);
    TOut visitSfuRtpSourceReport(TIn obj);
    TOut visitSfuRtpSinkReport(TIn obj);
    TOut visitSctpStreamReport(TIn obj);

}

