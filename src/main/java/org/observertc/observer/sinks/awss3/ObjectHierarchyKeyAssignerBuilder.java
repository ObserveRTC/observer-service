package org.observertc.observer.sinks.awss3;

import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportType;
import org.observertc.observer.reports.ReportTypeVisitor;
import org.observertc.schemas.reports.*;

import java.util.function.BiFunction;
import java.util.function.Function;

class ObjectHierarchyKeyAssignerBuilder {
    private static final String DELIMITER = "/";
    private static final String UNKNOWN_SERVICE = "unknown-services";
    private static final String UNKNOWN_CALL_ID = "unknown-calls";
    private static final String UNKNOWN_SFU_ID = "unknown-sfus";
    private static final String CALL_REPORTS = "call-reports";
    private static final String SFU_REPORTS = "sfu-reports";


    public Function<Report, String> create(Function<ReportType, String> typePrefixProvider,
                                           boolean addServiceIdPrefix,
                                           boolean addReportCategoryPrefix,
                                           boolean addSfuOrCallIdPrefix
                                           )
    {
        var wrongCallReportsBuf = new StringBuffer();
        var wrongSfuReportsBuf = new StringBuffer();
        if (addServiceIdPrefix) {
            wrongCallReportsBuf.append(UNKNOWN_SERVICE).append(DELIMITER);
            wrongSfuReportsBuf.append(UNKNOWN_SERVICE).append(DELIMITER);
        }
        if (addReportCategoryPrefix) {
            wrongCallReportsBuf.append(CALL_REPORTS).append(DELIMITER);
            wrongSfuReportsBuf.append(SFU_REPORTS).append(DELIMITER);
        }
        if (addSfuOrCallIdPrefix) {
            wrongCallReportsBuf.append(UNKNOWN_CALL_ID).append(DELIMITER);
            wrongSfuReportsBuf.append(UNKNOWN_SFU_ID).append(DELIMITER);
        }
        var wrongCallReports = wrongCallReportsBuf.toString();
        var wrongSfuReports = wrongSfuReportsBuf.toString();
        var OBSERVER_EVENT = typePrefixProvider.apply(ReportType.OBSERVER_EVENT);
        var CALL_EVENT = typePrefixProvider.apply(ReportType.CALL_EVENT);
        var CALL_META_DATA = typePrefixProvider.apply(ReportType.CALL_META_DATA);
        var CLIENT_EXTENSION_DATA = typePrefixProvider.apply(ReportType.CLIENT_EXTENSION_DATA);
        var PEER_CONNECTION_TRANSPORT = typePrefixProvider.apply(ReportType.PEER_CONNECTION_TRANSPORT);
        var PEER_CONNECTION_DATA_CHANNEL = typePrefixProvider.apply(ReportType.PEER_CONNECTION_DATA_CHANNEL);
        var INBOUND_AUDIO_TRACK = typePrefixProvider.apply(ReportType.INBOUND_AUDIO_TRACK);
        var INBOUND_VIDEO_TRACK = typePrefixProvider.apply(ReportType.INBOUND_VIDEO_TRACK);
        var OUTBOUND_AUDIO_TRACK = typePrefixProvider.apply(ReportType.OUTBOUND_AUDIO_TRACK);
        var OUTBOUND_VIDEO_TRACK = typePrefixProvider.apply(ReportType.OUTBOUND_VIDEO_TRACK);
        var SFU_EVENT = typePrefixProvider.apply(ReportType.SFU_EVENT);
        var SFU_META_DATA = typePrefixProvider.apply(ReportType.SFU_META_DATA);
        var SFU_EXTENSION_DATA = typePrefixProvider.apply(ReportType.SFU_EXTENSION_DATA);
        var SFU_TRANSPORT = typePrefixProvider.apply(ReportType.SFU_TRANSPORT);
        var SFU_INBOUND_RTP_PAD = typePrefixProvider.apply(ReportType.SFU_INBOUND_RTP_PAD);
        var SFU_OUTBOUND_RTP_PAD = typePrefixProvider.apply(ReportType.SFU_OUTBOUND_RTP_PAD);
        var SFU_SCTP_STREAM = typePrefixProvider.apply(ReportType.SFU_SCTP_STREAM);
        BiFunction<String, String, StringBuffer> createStringBufferForCallReports = (serviceId, callId) -> {
            var strbuf = new StringBuffer();
            if (addServiceIdPrefix) {
                strbuf.append(serviceId != null ? serviceId : UNKNOWN_SERVICE).append(DELIMITER);
            }
            if (addReportCategoryPrefix) {
                strbuf.append(CALL_REPORTS).append(DELIMITER);
            }
            if (addSfuOrCallIdPrefix) {
                strbuf.append(callId != null ? callId : UNKNOWN_CALL_ID).append(DELIMITER);
            }
            return strbuf;
        };
        BiFunction<String, String, StringBuffer> createStringBufferForSfuReports = (serviceId, sfuId) -> {
            var strbuf = new StringBuffer();
            if (addServiceIdPrefix) {
                strbuf.append(serviceId != null ? serviceId : UNKNOWN_SERVICE).append(DELIMITER);
            }
            if (addReportCategoryPrefix) {
                strbuf.append(SFU_REPORTS).append(DELIMITER);
            }
            if (addSfuOrCallIdPrefix) {
                strbuf.append(sfuId != null ? sfuId : UNKNOWN_SFU_ID).append(DELIMITER);
            }
            return strbuf;
        };
        ReportTypeVisitor<Report, String> visitor = new ReportTypeVisitor<Report, String>() {
            @Override
            public String visitObserverEventReport(Report obj) {
                try {
                    var payload = (ObserverEventReport) obj.payload;
                    return createStringBufferForCallReports.apply(payload.serviceId, payload.callId)
                            .append(OBSERVER_EVENT)
                            .toString();
                } catch (Exception ex) {
                    return wrongCallReports;
                }
            }

            @Override
            public String visitCallEventReport(Report obj) {
                try {
                    var payload = (CallEventReport) obj.payload;
                    return createStringBufferForCallReports.apply(payload.serviceId, payload.callId)
                            .append(CALL_EVENT)
                            .toString();
                } catch (Exception ex) {
                    return wrongCallReports;
                }
            }

            @Override
            public String visitCallMetaDataReport(Report obj) {
                try {
                    var payload = (CallMetaReport) obj.payload;
                    return createStringBufferForCallReports.apply(payload.serviceId, payload.callId)
                            .append(CALL_META_DATA)
                            .toString();
                } catch (Exception ex) {
                    return wrongCallReports;
                }
            }

            @Override
            public String visitClientExtensionDataReport(Report obj) {
                try {
                    var payload = (ClientExtensionReport) obj.payload;
                    return createStringBufferForCallReports.apply(payload.serviceId, payload.callId)
                            .append(CLIENT_EXTENSION_DATA)
                            .toString();
                } catch (Exception ex) {
                    return wrongCallReports;
                }
            }

            @Override
            public String visitClientTransportReport(Report obj) {
                try {
                    var payload = (ClientTransportReport) obj.payload;
                    return createStringBufferForCallReports.apply(payload.serviceId, payload.callId)
                            .append(PEER_CONNECTION_TRANSPORT)
                            .toString();
                } catch (Exception ex) {
                    return wrongCallReports;
                }
            }

            @Override
            public String visitClientDataChannelReport(Report obj) {
                try {
                    var payload = (ClientDataChannelReport) obj.payload;
                    return createStringBufferForCallReports.apply(payload.serviceId, payload.callId)
                            .append(PEER_CONNECTION_DATA_CHANNEL)
                            .toString();
                } catch (Exception ex) {
                    return wrongCallReports;
                }
            }

            @Override
            public String visitInboundAudioTrackReport(Report obj) {
                try {
                    var payload = (InboundAudioTrackReport) obj.payload;
                    return createStringBufferForCallReports.apply(payload.serviceId, payload.callId)
                            .append(INBOUND_AUDIO_TRACK)
                            .toString();
                } catch (Exception ex) {
                    return wrongCallReports;
                }
            }

            @Override
            public String visitInboundVideoTrackReport(Report obj) {
                try {
                    var payload = (InboundVideoTrackReport) obj.payload;
                    return createStringBufferForCallReports.apply(payload.serviceId, payload.callId)
                            .append(INBOUND_VIDEO_TRACK)
                            .toString();
                } catch (Exception ex) {
                    return wrongCallReports;
                }
            }

            @Override
            public String visitOutboundAudioTrackReport(Report obj) {
                try {
                    var payload = (OutboundAudioTrackReport) obj.payload;
                    return createStringBufferForCallReports.apply(payload.serviceId, payload.callId)
                            .append(OUTBOUND_AUDIO_TRACK)
                            .toString();
                } catch (Exception ex) {
                    return wrongCallReports;
                }
            }

            @Override
            public String visitOutboundVideoTrackReport(Report obj) {
                try {
                    var payload = (OutboundVideoTrackReport) obj.payload;
                    return createStringBufferForCallReports.apply(payload.serviceId, payload.callId)
                            .append(OUTBOUND_VIDEO_TRACK)
                            .toString();
                } catch (Exception ex) {
                    return wrongCallReports;
                }
            }

            @Override
            public String visitSfuEventReport(Report obj) {
                try {
                    var payload = (SfuEventReport) obj.payload;
                    return createStringBufferForSfuReports.apply(payload.serviceId, payload.sfuId)
                            .append(SFU_EVENT)
                            .toString();
                } catch (Exception ex) {
                    return wrongSfuReports;
                }
            }

            @Override
            public String visitSfuMetaReport(Report obj) {
                try {
                    var payload = (SfuMetaReport) obj.payload;
                    return createStringBufferForSfuReports.apply(payload.serviceId, payload.sfuId)
                            .append(SFU_META_DATA)
                            .toString();
                } catch (Exception ex) {
                    return wrongSfuReports;
                }
            }

            @Override
            public String visitSfuExtensionReport(Report obj) {
                try {
                    var payload = (SfuExtensionReport) obj.payload;
                    return createStringBufferForSfuReports.apply(payload.serviceId, payload.sfuId)
                            .append(SFU_EXTENSION_DATA)
                            .toString();
                } catch (Exception ex) {
                    return wrongSfuReports;
                }
            }

            @Override
            public String visitSfuTransportReport(Report obj) {
                try {
                    var payload = (SFUTransportReport) obj.payload;
                    return createStringBufferForSfuReports.apply(payload.serviceId, payload.sfuId)
                            .append(SFU_TRANSPORT)
                            .toString();
                } catch (Exception ex) {
                    return wrongSfuReports;
                }
            }

            @Override
            public String visitSfuInboundRtpPadReport(Report obj) {
                try {
                    var payload = (SfuInboundRtpPadReport) obj.payload;
                    return createStringBufferForSfuReports.apply(payload.serviceId, payload.sfuId)
                            .append(SFU_INBOUND_RTP_PAD)
                            .toString();
                } catch (Exception ex) {
                    return wrongSfuReports;
                }
            }

            @Override
            public String visitSfuOutboundRtpPadReport(Report obj) {
                try {
                    var payload = (SfuOutboundRtpPadReport) obj.payload;
                    return createStringBufferForSfuReports.apply(payload.serviceId, payload.sfuId)
                            .append(SFU_OUTBOUND_RTP_PAD)
                            .toString();
                } catch (Exception ex) {
                    return wrongSfuReports;
                }
            }

            @Override
            public String visitSctpStreamReport(Report obj) {
                try {
                    var payload = (SfuSctpStreamReport) obj.payload;
                    return createStringBufferForSfuReports.apply(payload.serviceId, payload.sfuId)
                            .append(SFU_SCTP_STREAM)
                            .toString();
                } catch (Exception ex) {
                    return wrongSfuReports;
                }
            }
        };
        return report -> visitor.apply(report, report.type);
    }

}
