package org.observertc.webrtc.observer;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.schemas.reports.*;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Prototype
public class ReportGenerators {
    private static final String ROOM_ID_FIELD_NAME = "roomId";
    private static final String USER_ID_FIELD_NAME = "userId";
    private static final String SAMPLE_SEQ_FIELD_NAME = "sampleSeq";

    @Inject
    RandomGenerators randomGenerators;

    private final EasyRandom callEventReportGenerator;
    private final EasyRandom callMetaReportGenerator;
    private final EasyRandom clientExtensionReportGenerator;
    private final EasyRandom clientTransportReportGenerator;
    private final EasyRandom ClientDataChannelReportGenerator;
    private final EasyRandom inboundAudioTrackGenerator;
    private final EasyRandom inboundVideoTrackGenerator;
    private final EasyRandom outboundAudioTrackGenerator;
    private final EasyRandom outboundVideoTrackGenerator;

    public ReportGenerators() {
        this.callEventReportGenerator = this.makeCallEventReportGenerator();
        this.callMetaReportGenerator = this.makeCallMetaReportGenerator();
        this.clientExtensionReportGenerator = this.makeClientExtensionReportGenerator();
        this.clientTransportReportGenerator = this.makeClientTransportReportGenerator();
        this.ClientDataChannelReportGenerator = this.makeClientDataChannelReportGenerator();
        this.inboundAudioTrackGenerator = this.makeInboundAudioTrackReportGenerator();
        this.inboundVideoTrackGenerator = this.makeInboundVideoTrackReportGenerator();
        this.outboundAudioTrackGenerator = this.makeOutboundAudioTrackReportGenerator();
        this.outboundVideoTrackGenerator = this.makeOutboundVideoTrackReportGenerator();
    }

    public CallEventReport getCallEventReport() {
        var result = this.callEventReportGenerator.nextObject(CallEventReport.class);
        return result;
    }

    public CallMetaReport getCallMetaReport() {
        var result = this.callMetaReportGenerator.nextObject(CallMetaReport.class);
        return result;
    }

    public ClientExtensionReport getClientExtensionReport() {
        var result = this.clientExtensionReportGenerator.nextObject(ClientExtensionReport.class);
        return result;
    }

    public ClientTransportReport getClientTransportReport() {
        var result = this.clientTransportReportGenerator.nextObject(ClientTransportReport.class);
        return result;
    }

    public ClientDataChannelReport getClientDataChannelReport() {
        var result = this.ClientDataChannelReportGenerator.nextObject(ClientDataChannelReport.class);
        return result;
    }

    public InboundAudioTrackReport getInboundAudioTrackReport() {
        var result = this.inboundAudioTrackGenerator.nextObject(InboundAudioTrackReport.class);
        return result;
    }

    public InboundVideoTrackReport getInboundVideoTrackReport() {
        var result = this.inboundVideoTrackGenerator.nextObject(InboundVideoTrackReport.class);
        return result;
    }

    public OutboundAudioTrackReport getOutboundAudioTrackReport() {
        var result = this.outboundAudioTrackGenerator.nextObject(OutboundAudioTrackReport.class);
        return result;
    }

    public OutboundVideoTrackReport getOutboundVideoTrackReport() {
        var result = this.outboundVideoTrackGenerator.nextObject(OutboundVideoTrackReport.class);
        return result;
    }

    private EasyRandom makeCallEventReportGenerator() {
        var parameters = this.getBaseParameters();
        Predicate<Field> typeField = field -> field.getName().toLowerCase().contains("type");
        List<CallEventType> callEventTypes = Arrays.asList(CallEventType.values());
        parameters
                .randomize(typeField, () -> this.randomGenerators.getRandomFromList(callEventTypes).toString())
                ;

        return new EasyRandom(parameters);
    }

    private EasyRandom makeCallMetaReportGenerator() {
        var parameters = this.getBaseParameters();
        return new EasyRandom(parameters);
    }

    private EasyRandom makeClientExtensionReportGenerator() {
        var parameters = this.getBaseParameters();
        return new EasyRandom(parameters);
    }

    private EasyRandom makeClientTransportReportGenerator() {
        var parameters = this.getBaseParameters();
        return new EasyRandom(parameters);
    }

    private EasyRandom makeClientDataChannelReportGenerator() {
        var parameters = this.getBaseParameters();
        return new EasyRandom(parameters);
    }

    private EasyRandom makeInboundAudioTrackReportGenerator() {
        var parameters = this.getBaseParameters();
        return new EasyRandom(parameters);
    }

    private EasyRandom makeInboundVideoTrackReportGenerator() {
        var parameters = this.getBaseParameters();
        return new EasyRandom(parameters);
    }

    private EasyRandom makeOutboundAudioTrackReportGenerator() {
        var parameters = this.getBaseParameters();
        return new EasyRandom(parameters);
    }

    private EasyRandom makeOutboundVideoTrackReportGenerator() {
        var parameters = this.getBaseParameters();
        return new EasyRandom(parameters);
    }



    private EasyRandomParameters getBaseParameters() {
        Predicate<Field> uuidFields = field -> {
            String fieldName = field.getName();
            return fieldName.contains("Id") &&
                    !fieldName.equals(ROOM_ID_FIELD_NAME) &&
                    !fieldName.equals(USER_ID_FIELD_NAME);
        };
        Predicate<Field> roomIdField = field -> field.getName().equals(ROOM_ID_FIELD_NAME);
        Predicate<Field> userIdField = field -> field.getName().equals(USER_ID_FIELD_NAME);
        Predicate<Field> sampleSeqField = field -> field.getName().equals(SAMPLE_SEQ_FIELD_NAME);
        Predicate<Field> timestampField = field -> field.getName().toLowerCase().contains("timestamp");
        List<String> testRoomIds = TestUtils.getTestRoomIds();
        List<String> testUserIds = TestUtils.getTestUserIds();

        EasyRandomParameters result = new EasyRandomParameters()
                .randomize(uuidFields, () -> UUID.randomUUID().toString())
                .randomize(roomIdField, () -> this.randomGenerators.getRandomFromList(testRoomIds))
                .randomize(userIdField, () -> this.randomGenerators.getRandomFromList(testUserIds))
                .randomize(sampleSeqField, () -> this.randomGenerators.getRandomPositiveInteger())
                .randomize(timestampField, () -> this.randomGenerators.getRandomTimestamp())
                ;
        return result;
    }

}
