package org.observertc.observer.reports;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

class ReportTypeVisitorTest {

    @Test
    void shouldBeCorrectCallInvocationsForFunctions() {
        var visitor = ReportTypeVisitor.<Void, ReportType>createFunctionalVisitor(
                VOID -> ReportType.OBSERVER_EVENT,
                VOID -> ReportType.CALL_EVENT,
                VOID -> ReportType.CALL_META_DATA,
                VOID -> ReportType.CLIENT_EXTENSION_DATA,
                VOID -> ReportType.PEER_CONNECTION_TRANSPORT,
                VOID -> ReportType.PEER_CONNECTION_DATA_CHANNEL,
                VOID -> ReportType.INBOUND_AUDIO_TRACK,
                VOID -> ReportType.INBOUND_VIDEO_TRACK,
                VOID -> ReportType.OUTBOUND_AUDIO_TRACK,
                VOID -> ReportType.OUTBOUND_VIDEO_TRACK,
                VOID -> ReportType.SFU_EVENT,
                VOID -> ReportType.SFU_META_DATA,
                VOID -> ReportType.SFU_EXTENSION_DATA,
                VOID -> ReportType.SFU_TRANSPORT,
                VOID -> ReportType.SFU_INBOUND_RTP_PAD,
                VOID -> ReportType.SFU_OUTBOUND_RTP_PAD,
                VOID -> ReportType.SFU_SCTP_STREAM
        );
        for (var expected : ReportType.values()) {
            var actual = visitor.apply(null, expected);
            Assertions.assertEquals(expected, actual);
        }
    }

    @Test
    void shouldBeCorrectCallInvocationsForConsumers() {
        var visitor = ReportTypeVisitor.<ReportType>createConsumerVisitor(
                actual -> Assertions.assertEquals(ReportType.OBSERVER_EVENT, actual),
                actual -> Assertions.assertEquals(ReportType.CALL_EVENT, actual),
                actual -> Assertions.assertEquals(ReportType.CALL_META_DATA, actual),
                actual -> Assertions.assertEquals(ReportType.CLIENT_EXTENSION_DATA, actual),
                actual -> Assertions.assertEquals(ReportType.PEER_CONNECTION_TRANSPORT, actual),
                actual -> Assertions.assertEquals(ReportType.PEER_CONNECTION_DATA_CHANNEL, actual),
                actual -> Assertions.assertEquals(ReportType.INBOUND_AUDIO_TRACK, actual),
                actual -> Assertions.assertEquals(ReportType.INBOUND_VIDEO_TRACK, actual),
                actual -> Assertions.assertEquals(ReportType.OUTBOUND_AUDIO_TRACK, actual),
                actual -> Assertions.assertEquals(ReportType.OUTBOUND_VIDEO_TRACK, actual),
                actual -> Assertions.assertEquals(ReportType.SFU_EVENT, actual),
                actual -> Assertions.assertEquals(ReportType.SFU_META_DATA, actual),
                actual -> Assertions.assertEquals(ReportType.SFU_EXTENSION_DATA, actual),
                actual -> Assertions.assertEquals(ReportType.SFU_TRANSPORT, actual),
                actual -> Assertions.assertEquals(ReportType.SFU_INBOUND_RTP_PAD, actual),
                actual -> Assertions.assertEquals(ReportType.SFU_OUTBOUND_RTP_PAD, actual),
                actual -> Assertions.assertEquals(ReportType.SFU_SCTP_STREAM, actual)
        );

        for (var expected : ReportType.values()) {
            var actual = visitor.apply(expected, expected);
        }
    }

    @Test
    void shouldBeCorrectCallInvocationsForSuppliers() {
        var visitor = ReportTypeVisitor.<ReportType>createSupplierVisitor(
                () -> ReportType.OBSERVER_EVENT,
                () -> ReportType.CALL_EVENT,
                () -> ReportType.CALL_META_DATA,
                () -> ReportType.CLIENT_EXTENSION_DATA,
                () -> ReportType.PEER_CONNECTION_TRANSPORT,
                () -> ReportType.PEER_CONNECTION_DATA_CHANNEL,
                () -> ReportType.INBOUND_AUDIO_TRACK,
                () -> ReportType.INBOUND_VIDEO_TRACK,
                () -> ReportType.OUTBOUND_AUDIO_TRACK,
                () -> ReportType.OUTBOUND_VIDEO_TRACK,
                () -> ReportType.SFU_EVENT,
                () -> ReportType.SFU_META_DATA,
                () -> ReportType.SFU_EXTENSION_DATA,
                () -> ReportType.SFU_TRANSPORT,
                () -> ReportType.SFU_INBOUND_RTP_PAD,
                () -> ReportType.SFU_OUTBOUND_RTP_PAD,
                () -> ReportType.SFU_SCTP_STREAM
        );
        for (var expected : ReportType.values()) {
            var actual = visitor.apply(null, expected);
            Assertions.assertEquals(expected, actual);
        }
    }

    @Test
    void shouldBeCorrectCallInvocationsForRunnable() {
        AtomicReference<ReportType> actual = new AtomicReference<>(null);
        var visitor = ReportTypeVisitor.createRunnableVisitor(
                () -> Assertions.assertEquals(ReportType.OBSERVER_EVENT, actual.get()),
                () -> Assertions.assertEquals(ReportType.CALL_EVENT, actual.get()),
                () -> Assertions.assertEquals(ReportType.CALL_META_DATA, actual.get()),
                () -> Assertions.assertEquals(ReportType.CLIENT_EXTENSION_DATA, actual.get()),
                () -> Assertions.assertEquals(ReportType.PEER_CONNECTION_TRANSPORT, actual.get()),
                () -> Assertions.assertEquals(ReportType.PEER_CONNECTION_DATA_CHANNEL, actual.get()),
                () -> Assertions.assertEquals(ReportType.INBOUND_AUDIO_TRACK, actual.get()),
                () -> Assertions.assertEquals(ReportType.INBOUND_VIDEO_TRACK, actual.get()),
                () -> Assertions.assertEquals(ReportType.OUTBOUND_AUDIO_TRACK, actual.get()),
                () -> Assertions.assertEquals(ReportType.OUTBOUND_VIDEO_TRACK, actual.get()),
                () -> Assertions.assertEquals(ReportType.SFU_EVENT, actual.get()),
                () -> Assertions.assertEquals(ReportType.SFU_META_DATA, actual.get()),
                () -> Assertions.assertEquals(ReportType.SFU_EXTENSION_DATA, actual.get()),
                () -> Assertions.assertEquals(ReportType.SFU_TRANSPORT, actual.get()),
                () -> Assertions.assertEquals(ReportType.SFU_INBOUND_RTP_PAD, actual.get()),
                () -> Assertions.assertEquals(ReportType.SFU_OUTBOUND_RTP_PAD, actual.get()),
                () -> Assertions.assertEquals(ReportType.SFU_SCTP_STREAM, actual.get())
        );

        for (var reportType : ReportType.values()) {
            actual.set(reportType);
            visitor.apply(null, reportType);
        }
    }
}