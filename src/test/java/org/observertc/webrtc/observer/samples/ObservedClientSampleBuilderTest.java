package org.observertc.webrtc.observer.samples;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.InvalidObjectException;

@MicronautTest
class ObservedClientSampleBuilderTest {

    @Inject
    ClientSampleGenerator clientSampleGenerator;

    @Test
    public void invalidClientId() {
        var clientSample = this.clientSampleGenerator.get();

        clientSample.clientId = "Invalid UUID";

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ObservedClientSampleBuilder.from(clientSample).build();
        });
    }

    @Test
    public void noClientId() {
        var clientSample = this.clientSampleGenerator.get();

        clientSample.clientId = null;

        Assertions.assertThrows(NullPointerException.class, () -> {
            ObservedClientSampleBuilder.from(clientSample).build();
        });
    }

    @Test
    public void noTimestamp() {
        var clientSample = this.clientSampleGenerator.get();

        clientSample.timestamp = null;

        Assertions.assertThrows(NullPointerException.class, () -> {
            ObservedClientSampleBuilder
                    .from(clientSample)
                    .withServiceId("serviceId")
                    .withMediaUnitId("mediaUnitId")
                    .build();
        });
    }

    @Test
    public void noRoomId() {
        var clientSample = this.clientSampleGenerator.get();

        clientSample.roomId = null;

        Assertions.assertThrows(NullPointerException.class, () -> {
            ObservedClientSampleBuilder
                    .from(clientSample)
                    .withServiceId("serviceId")
                    .withMediaUnitId("mediaUnitId")
                    .build();
        });
    }

    @Test
    public void noMediaUnitId() {
        var clientSample = this.clientSampleGenerator.get();

        clientSample.roomId = null;

        Assertions.assertThrows(NullPointerException.class, () -> {
            ObservedClientSampleBuilder
                    .from(clientSample)
                    .withServiceId("serviceId")
//                    .withMediaUnitId("mediaUnitId")
                    .build();
        });
    }

    @Test
    public void noServiceId() {
        var clientSample = this.clientSampleGenerator.get();

        clientSample.roomId = null;

        Assertions.assertThrows(NullPointerException.class, () -> {
            ObservedClientSampleBuilder
                    .from(clientSample)
//                    .withServiceId("serviceId")
                    .withMediaUnitId("mediaUnitId")
                    .build();
        });
    }

    @Test
    public void getClientSample() throws InvalidObjectException {
        var clientSample = this.clientSampleGenerator.get();

        var observedClientSample = ObservedClientSampleBuilder
                .from(clientSample)
                .withServiceId("serviceId")
                .withMediaUnitId("mediaUnitId")
                .build();

        Assertions.assertEquals(clientSample, observedClientSample.getClientSample());
    }

    @Test
    public void getRoomId() throws InvalidObjectException {
        var clientSample = this.clientSampleGenerator.get();

        var observedClientSample = ObservedClientSampleBuilder
                .from(clientSample)
                .withServiceId("serviceId")
                .withMediaUnitId("mediaUnitId")
                .build();

        Assertions.assertEquals(clientSample.roomId, observedClientSample.getRoomId());
    }

    @Test
    public void getServiceId() throws InvalidObjectException {
        var clientSample = this.clientSampleGenerator.get();
        var serviceId = "serviceId";

        var observedClientSample = ObservedClientSampleBuilder
                .from(clientSample)
                .withServiceId(serviceId)
                .withMediaUnitId("mediaUnitId")
                .build();

        Assertions.assertEquals(serviceId, observedClientSample.getServiceId());
    }

    @Test
    public void getMediaUnitId() throws InvalidObjectException {
        var clientSample = this.clientSampleGenerator.get();
        var mediaUnitId = "mediaUnitId";

        var observedClientSample = ObservedClientSampleBuilder
                .from(clientSample)
                .withServiceId("serviceId")
                .withMediaUnitId(mediaUnitId)
                .build();

        Assertions.assertEquals(mediaUnitId, observedClientSample.getMediaUnitId());
    }

    @Test
    public void getTimestamp() throws InvalidObjectException {
        var clientSample = this.clientSampleGenerator.get();

        var observedClientSample = ObservedClientSampleBuilder
                .from(clientSample)
                .withServiceId("serviceId")
                .withMediaUnitId("mediaUnitId")
                .build();

        Assertions.assertEquals(clientSample.timestamp, observedClientSample.getTimestamp());
    }

    @Test
    public void getUserId() throws InvalidObjectException {
        var clientSample = this.clientSampleGenerator.get();

        var observedClientSample = ObservedClientSampleBuilder
                .from(clientSample)
                .withServiceId("serviceId")
                .withMediaUnitId("mediaUnitId")
                .build();

        Assertions.assertEquals(clientSample.userId, observedClientSample.getUserId());
    }

    @Test
    public void getTimeZoneId() throws InvalidObjectException {
        var clientSample = this.clientSampleGenerator.get();
        var timeZoneId = "timeZoneId";

        var observedClientSample = ObservedClientSampleBuilder
                .from(clientSample)
                .withServiceId("serviceId")
                .withMediaUnitId("mediaUnitId")
                .withTimeZoneId(timeZoneId)
                .build();

        Assertions.assertEquals(timeZoneId, observedClientSample.getTimeZoneId());
    }
}