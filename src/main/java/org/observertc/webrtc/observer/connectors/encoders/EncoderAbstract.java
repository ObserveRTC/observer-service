package org.observertc.webrtc.observer.connectors.encoders;

import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.observertc.webrtc.observer.connectors.MessageFormat;
import org.observertc.webrtc.observer.connectors.encoders.avro.AvroEncoder;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class EncoderAbstract<T> implements Encoder {

    private static final Logger logger = LoggerFactory.getLogger(AvroEncoder.class);

    private BiConsumer<EncodedRecord.Builder, Report> metaBuilder = (e, r) -> {};
    private Function<T, Object> convert;
    private MessageFormat format;
    public EncoderAbstract() {
        this.withMessageFormat(MessageFormat.OBJECT);
    }

    @Override
    public EncodedRecord apply(Report report) throws Throwable {
        T recordItem;
        try {
            recordItem = this.make(report);
        } catch (Throwable t) {
            logger.error("An error occurred during conversion", t);
            return EncodedRecord.ofEmpty();
        }
        if (Objects.isNull(recordItem)) {
            return EncodedRecord.ofEmpty();
        }

        EncodedRecord.Builder recordBuilder = EncodedRecord.builder();
        try {
            Object message = this.convert.apply(recordItem);
            recordBuilder
                    .withMessage(message)
                    .withFormat(this.format)
                    .withEncoderType(this.getClass());
        } catch (Throwable t) {
            logger.error("An error occurred during formatting", t);
            return EncodedRecord.ofEmpty();
        }

        try {
            this.metaBuilder.accept(recordBuilder, report);
        } catch (Throwable t) {
            logger.error("An error occurred during metabuilder process", t);
        }

        return recordBuilder.build();
    }

    public EncoderAbstract withMetaBuilder(BiConsumer<EncodedRecord.Builder, Report> metaBuilder) {
        this.metaBuilder = this.metaBuilder.andThen(metaBuilder);
        return this;
    }

    public EncoderAbstract<T> withMessageFormat(MessageFormat format) {
        this.format = format;
        switch (this.format) {
            case BYTES:
                this.convert = this::convertToBytes;
                break;
            default:
            case OBJECT:
                this.convert = obj -> obj;
                break;
        }
        return this;
    }

    protected abstract T make(Report report) throws Throwable;

    protected abstract byte[] convertToBytes(T object) throws Throwable;

}
