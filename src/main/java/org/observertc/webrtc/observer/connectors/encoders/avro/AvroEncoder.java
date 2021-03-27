package org.observertc.webrtc.observer.connectors.encoders.avro;

import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.observertc.webrtc.observer.connectors.encoders.Encoder;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

public class AvroEncoder implements Encoder {
    private static final Logger logger = LoggerFactory.getLogger(AvroEncoder.class);

    private BiConsumer<EncodedRecord.Builder, Report> metaBuilder = (e, r) -> {};

    @Override
    public EncodedRecord apply(Report report) throws Throwable {
        ByteBuffer message;
        try {
            message = Report.getEncoder().encode(report);
        } catch (Throwable t) {
            logger.warn("Cannot encode report {}", report, t);
            return null;
        }
        if (!message.hasArray()) {
            logger.warn("Failed to encode report {}", report);
            return null;
        }
        EncodedRecord.Builder recordBuilder = EncodedRecord.builder();
        recordBuilder.withMessage(message.array());
        this.metaBuilder.accept(recordBuilder, report);
        return recordBuilder.build();
    }

    AvroEncoder withMetaBuilder(BiConsumer<EncodedRecord.Builder, Report> metaBuilder) {
        this.metaBuilder = this.metaBuilder.andThen(metaBuilder);
        return this;
    }
}
