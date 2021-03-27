package org.observertc.webrtc.observer.connectors.encoders.jackson;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.observertc.webrtc.observer.connectors.encoders.Encoder;
import org.observertc.webrtc.observer.connectors.encoders.ReportKeyMaker;
import org.observertc.webrtc.observer.connectors.encoders.ReportMapper;
import org.observertc.webrtc.observer.connectors.encoders.SchemaMapper;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

@Prototype
public class JacksonEncoderBuilder  extends AbstractBuilder implements Builder<Encoder> {
    private final Logger logger = LoggerFactory.getLogger(JacksonEncoderBuilder.class);
    private final SchemaMapper schemaMapper = new SchemaMapper();

    @Override
    public Encoder build() {
        if (!schemaMapper.execute().succeeded()) {
            logger.error("Cannot make schema mapper, the encoder built is failed");
            return null;
        }
        Map<ReportType, ReportMapper> reportMappers = schemaMapper.getResult();
        JacksonEncoder result = new JacksonEncoder(reportMappers);
        JacksonEncoderConfig config = this.convertAndValidate(JacksonEncoderConfig.class);
        return result;
    }

    BiConsumer<EncodedRecord.Builder, Report> makeMetaKeyMaker() {
        final ReportKeyMaker keyMaker = new ReportKeyMaker();
        return new BiConsumer<EncodedRecord.Builder, Report>() {
            @Override
            public void accept(EncodedRecord.Builder recordBuilder, Report report) {
                UUID key = keyMaker.apply(report);
                recordBuilder.withKey(key);
            }
        };
    }

    public static class JacksonEncoderConfig {
        public boolean addMetaKey = true;
    }
}
