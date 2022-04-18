package org.observertc.observer.sources;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.configs.TransportFormatType;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class AcceptorTest {
    private static final Logger logger = LoggerFactory.getLogger(SamplesDecoderTest.class);

    private static final String MEDIA_UNIT_ID = "mediaUnitId";
    private static final String SERVICE_ID = "serviceId";

    @Test
    void shouldNotBuiltWithInvalidVersion() {
        Assertions.assertThrows(Throwable.class, () -> {
            var acceptor = Acceptor.create(
                    logger,
                    MEDIA_UNIT_ID,
                    SERVICE_ID,
                    "invalidVersion",
                    TransportFormatType.JSON,
                    receivedSamples -> {}
            );
        });
    }

    @Test
    void shouldTriggerErrorForInvalidMessage() throws InterruptedException, ExecutionException, TimeoutException {
        var completableFuture = new CompletableFuture();
        var acceptor = Acceptor.create(
                logger,
                MEDIA_UNIT_ID,
                SERVICE_ID,
                Samples.VERSION,
                TransportFormatType.PROTOBUF,
                receivedSamples -> {}
        ).onError(completableFuture::complete);
        acceptor.accept("notValid".getBytes(StandardCharsets.UTF_8));
        Assertions.assertNotNull(completableFuture.get(10000, TimeUnit.MILLISECONDS));
    }

    @Test
    void gettersShouldBeOk() {
        var acceptor = Acceptor.create(
                logger,
                MEDIA_UNIT_ID,
                SERVICE_ID,
                Samples.VERSION,
                TransportFormatType.PROTOBUF,
                receivedSamples -> {}
        );

        Assertions.assertEquals(MEDIA_UNIT_ID, acceptor.getMediaUnitId());
        Assertions.assertEquals(SERVICE_ID, acceptor.getServiceId());
    }
}