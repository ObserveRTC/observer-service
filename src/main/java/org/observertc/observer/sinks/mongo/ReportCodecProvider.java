package org.observertc.observer.sinks.mongo;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportType;
import org.observertc.observer.reports.ReportTypeVisitor;
import org.observertc.schemas.reports.*;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class ReportCodecProvider implements CodecProvider {
    private final PojoCodecProvider pojoCodecProvider;
    private final CodecRegistry pojoCodecRegistry;

    public ReportCodecProvider() {
        this.pojoCodecProvider = PojoCodecProvider.builder().register(
                ClassModel.builder(ObserverEventReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(CallEventReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(CallMetaReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(ClientExtensionReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(ClientTransportReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(ClientDataChannelReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(InboundAudioTrackReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(InboundVideoTrackReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(OutboundAudioTrackReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(OutboundVideoTrackReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(SfuEventReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(SfuMetaReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(SfuExtensionReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(SFUTransportReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(SfuInboundRtpPadReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(SfuOutboundRtpPadReport.class).enableDiscriminator(true).build(),
                ClassModel.builder(SfuSctpStreamReport.class).enableDiscriminator(true).build()
        ).build();
        this.pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz != Report.class) {
            return pojoCodecRegistry.get(clazz);
        }
        var reportTypeEncoder = this.makeReportTypeEncoder();
        var reportTypeDecoder = this.makeReportTypeDecoder();
        return (Codec<T>) new Codec<Report>() {

            @Override
            public void encode(BsonWriter writer, Report value, EncoderContext encoderContext) {
                reportTypeEncoder.apply(value.payload, value.type).accept(writer, encoderContext);
            }

            @Override
            public Class<Report> getEncoderClass() {
                return Report.class;
            }

            @Override
            public Report decode(BsonReader reader, DecoderContext decoderContext) {
                Report result = new Report();
                result.type = ReportType.valueOf(reader.readString("type"));
                reader.readStartDocument();
                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    String fieldName = reader.readName();
                    if (!fieldName.equals("payload")) continue;
                    result.payload = reportTypeDecoder.apply(null, result.type).apply(reader, decoderContext);
                    break;
                }
                reader.readEndDocument();
                return result;
            }
        };
    }

    private interface ReportEncoder extends BiConsumer<BsonWriter, EncoderContext> {
    }

    private interface ReportDecoder extends BiFunction<BsonReader, DecoderContext, Object> {
    }

    private ReportTypeVisitor<Object, ReportEncoder> makeReportTypeEncoder() {
        var observerEventReportCodec = pojoCodecRegistry.get(ObserverEventReport.class);
        var callEventReportCodec = pojoCodecRegistry.get(CallEventReport.class);
        var callMetaReportCodec = pojoCodecRegistry.get(CallMetaReport.class);
        var clientExtensionReportCodec = pojoCodecRegistry.get(ClientExtensionReport.class);
        var clientTransportReportCodec = pojoCodecRegistry.get(ClientTransportReport.class);
        var clientDataChannelReportCodec = pojoCodecRegistry.get(ClientDataChannelReport.class);
        var inboundAudioTrackReportCodec = pojoCodecRegistry.get(InboundAudioTrackReport.class);
        var inboundVideoTrackReportCodec = pojoCodecRegistry.get(InboundVideoTrackReport.class);
        var outboundAudioTrackReportCodec = pojoCodecRegistry.get(OutboundAudioTrackReport.class);
        var outboundVideoTrackReportCodec = pojoCodecRegistry.get(OutboundVideoTrackReport.class);
        var sfuEventReportCodec = pojoCodecRegistry.get(SfuEventReport.class);
        var sfuMetaReportCodec = pojoCodecRegistry.get(SfuMetaReport.class);
        var sfuExtensionReportCodec = pojoCodecRegistry.get(SfuExtensionReport.class);
        var sfuTransportReportCodec = pojoCodecRegistry.get(SFUTransportReport.class);
        var sfuInboundRtpPadReportCodec = pojoCodecRegistry.get(SfuInboundRtpPadReport.class);
        var sfuOutboundRtpPadReportCodec = pojoCodecRegistry.get(SfuOutboundRtpPadReport.class);
        var sfuSctpStreamReportCodec = pojoCodecRegistry.get(SfuSctpStreamReport.class);
        return ReportTypeVisitor.createFunctionalVisitor(
                (payload) -> (writer, context) -> {
                    observerEventReportCodec.encode(writer, (ObserverEventReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    callEventReportCodec.encode(writer, (CallEventReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    callMetaReportCodec.encode(writer, (CallMetaReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    clientExtensionReportCodec.encode(writer, (ClientExtensionReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    clientTransportReportCodec.encode(writer, (ClientTransportReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    clientDataChannelReportCodec.encode(writer, (ClientDataChannelReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    inboundAudioTrackReportCodec.encode(writer, (InboundAudioTrackReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    inboundVideoTrackReportCodec.encode(writer, (InboundVideoTrackReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    outboundAudioTrackReportCodec.encode(writer, (OutboundAudioTrackReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    outboundVideoTrackReportCodec.encode(writer, (OutboundVideoTrackReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    sfuEventReportCodec.encode(writer, (SfuEventReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    sfuMetaReportCodec.encode(writer, (SfuMetaReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    sfuExtensionReportCodec.encode(writer, (SfuExtensionReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    sfuTransportReportCodec.encode(writer, (SFUTransportReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    sfuInboundRtpPadReportCodec.encode(writer, (SfuInboundRtpPadReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    sfuOutboundRtpPadReportCodec.encode(writer, (SfuOutboundRtpPadReport) payload, context);
                },
                (payload) -> (writer, context) -> {
                    sfuSctpStreamReportCodec.encode(writer, (SfuSctpStreamReport) payload, context);
                }
        );
    }

    private ReportTypeVisitor<Object, ReportDecoder> makeReportTypeDecoder() {
        var observerEventReportCodec = pojoCodecRegistry.get(ObserverEventReport.class);
        var callEventReportCodec = pojoCodecRegistry.get(CallEventReport.class);
        var callMetaReportCodec = pojoCodecRegistry.get(CallMetaReport.class);
        var clientExtensionReportCodec = pojoCodecRegistry.get(ClientExtensionReport.class);
        var clientTransportReportCodec = pojoCodecRegistry.get(ClientTransportReport.class);
        var clientDataChannelReportCodec = pojoCodecRegistry.get(ClientDataChannelReport.class);
        var inboundAudioTrackReportCodec = pojoCodecRegistry.get(InboundAudioTrackReport.class);
        var inboundVideoTrackReportCodec = pojoCodecRegistry.get(InboundVideoTrackReport.class);
        var outboundAudioTrackReportCodec = pojoCodecRegistry.get(OutboundAudioTrackReport.class);
        var outboundVideoTrackReportCodec = pojoCodecRegistry.get(OutboundVideoTrackReport.class);
        var sfuEventReportCodec = pojoCodecRegistry.get(SfuEventReport.class);
        var sfuMetaReportCodec = pojoCodecRegistry.get(SfuMetaReport.class);
        var sfuExtensionReportCodec = pojoCodecRegistry.get(SfuExtensionReport.class);
        var sfuTransportReportCodec = pojoCodecRegistry.get(SFUTransportReport.class);
        var sfuInboundRtpPadReportCodec = pojoCodecRegistry.get(SfuInboundRtpPadReport.class);
        var sfuOutboundRtpPadReportCodec = pojoCodecRegistry.get(SfuOutboundRtpPadReport.class);
        var sfuSctpStreamReportCodec = pojoCodecRegistry.get(SfuSctpStreamReport.class);
        return ReportTypeVisitor.createFunctionalVisitor(
                (payload) -> (reader, context) -> {
                    return observerEventReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return callEventReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return callMetaReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return clientExtensionReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return clientTransportReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return clientDataChannelReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return inboundAudioTrackReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return inboundVideoTrackReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return outboundAudioTrackReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return outboundVideoTrackReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return sfuEventReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return sfuMetaReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return sfuExtensionReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return sfuTransportReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return sfuInboundRtpPadReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return sfuOutboundRtpPadReportCodec.decode(reader, context);
                },
                (payload) -> (reader, context) -> {
                    return sfuSctpStreamReportCodec.decode(reader, context);
                }
        );
    }
}
