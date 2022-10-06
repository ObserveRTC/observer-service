package org.observertc.observer.reports;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.ReportGenerators;

class ReportTest {

    private static final ReportGenerators generator = new ReportGenerators();

    @Test
    public void shouldGenerateFromObserverEventReport() {
        var payload = generator.generateObserverEventReport();
        var report = Report.fromObserverEventReport(payload);

        Assertions.assertEquals(ReportType.OBSERVER_EVENT, report.type);
    }

    @Test
    public void shouldGenerateFromCallEventReport() {
        var payload = generator.generateCallEventReport();
        var report = Report.fromCallEventReport(payload);

        Assertions.assertEquals(ReportType.CALL_EVENT, report.type);
    }

    @Test
    public void shouldGenerateFromCallMetaReport() {
        var payload = generator.generateCallMetaReport();
        var report = Report.fromCallMetaReport(payload);

        Assertions.assertEquals(ReportType.CALL_META_DATA, report.type);
    }

    @Test
    public void shouldGenerateFromClientExtensionReport() {
        var payload = generator.generateClientExtensionReport();
        var report = Report.fromClientExtensionReport(payload);

        Assertions.assertEquals(ReportType.CLIENT_EXTENSION_DATA, report.type);
    }

    @Test
    public void shouldGenerateFromPeerConnectionTransportReport() {
        var payload = generator.generatePeerConnectionTransport();
        var report = Report.fromPeerConnectionTransportReport(payload);

        Assertions.assertEquals(ReportType.PEER_CONNECTION_TRANSPORT, report.type);
    }

    @Test
    public void shouldGenerateFromIceCandidatePairReport() {
        var payload = generator.generateIceCandidatePairReport();
        var report = Report.fromIceCandidatePairReport(payload);

        Assertions.assertEquals(ReportType.ICE_CANDIDATE_PAIR, report.type);
    }

    @Test
    public void shouldGenerateFromClientDataChannelReport() {
        var payload = generator.generateClientDataChannelReport();
        var report = Report.fromClientDataChannelReport(payload);

        Assertions.assertEquals(ReportType.PEER_CONNECTION_DATA_CHANNEL, report.type);
    }

    @Test
    public void shouldGenerateFromInboundAudioTrackReport() {
        var payload = generator.generateInboundAudioTrackReport();
        var report = Report.fromInboundAudioTrackReport(payload);

        Assertions.assertEquals(ReportType.INBOUND_AUDIO_TRACK, report.type);
    }

    @Test
    public void shouldGenerateFromInboundVideoTrackReport() {
        var payload = generator.generateInboundVideoTrackReport();
        var report = Report.fromInboundVideoTrackReport(payload);

        Assertions.assertEquals(ReportType.INBOUND_VIDEO_TRACK, report.type);
    }

    @Test
    public void shouldGenerateFromOutboundAudioTrackReport() {
        var payload = generator.getnerateOutboundAudioTrackReport();
        var report = Report.fromOutboundAudioTrackReport(payload);

        Assertions.assertEquals(ReportType.OUTBOUND_AUDIO_TRACK, report.type);
    }

    @Test
    public void shouldGenerateFromOutboundVideoTrackReport() {
        var payload = generator.generateOutboundVideoTrackReport();
        var report = Report.fromOutboundVideoTrackReport(payload);

        Assertions.assertEquals(ReportType.OUTBOUND_VIDEO_TRACK, report.type);
    }

    @Test
    public void shouldGenerateFromSfuEventReport() {
        var payload = generator.generateSfuEventReport();
        var report = Report.fromSfuEventReport(payload);

        Assertions.assertEquals(ReportType.SFU_EVENT, report.type);
    }

    @Test
    public void shouldGenerateFromSfuMetaReport() {
        var payload = generator.generateSfuMetaReport();
        var report = Report.fromSfuMetaReport(payload);

        Assertions.assertEquals(ReportType.SFU_META_DATA, report.type);
    }

    @Test
    public void shouldGenerateFromSfuExtensionReport() {
        var payload = generator.generateSfuExtensionReport();
        var report = Report.fromSfuExtensionReport(payload);

        Assertions.assertEquals(ReportType.SFU_EXTENSION_DATA, report.type);
    }

    @Test
    public void shouldGenerateFromSfuTransportReport() {
        var payload = generator.generateSfuTransportReport();
        var report = Report.fromSfuTransportReport(payload);

        Assertions.assertEquals(ReportType.SFU_TRANSPORT, report.type);
    }

    @Test
    public void shouldGenerateFromSfuInboundRtpPadReport() {
        var payload = generator.generateSfuInboundRtpPadReport();
        var report = Report.fromSfuInboundRtpPadReport(payload);

        Assertions.assertEquals(ReportType.SFU_INBOUND_RTP_PAD, report.type);
    }

    @Test
    public void shouldGenerateFromSfuOutboundRtpPadReport() {
        var payload = generator.generateSfuOutboundRtpPadReport();
        var report = Report.fromSfuOutboundRtpPadReport(payload);

        Assertions.assertEquals(ReportType.SFU_OUTBOUND_RTP_PAD, report.type);
    }

    @Test
    public void shouldGenerateFromSfuSctpStreamReport() {
        var payload = generator.generateSfuSctpStreamReport();
        var report = Report.fromSfuSctpStreamReport(payload);

        Assertions.assertEquals(ReportType.SFU_SCTP_STREAM, report.type);
    }
}