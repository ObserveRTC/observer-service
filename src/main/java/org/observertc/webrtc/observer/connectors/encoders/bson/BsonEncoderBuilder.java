package org.observertc.webrtc.observer.connectors.encoders.bson;

import io.micronaut.context.annotation.Prototype;
import org.bson.Document;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.observertc.webrtc.observer.connectors.encoders.Encoder;
import org.observertc.webrtc.observer.connectors.encoders.ReportKeyMaker;
import org.observertc.webrtc.observer.connectors.encoders.ReportMapperAbstract;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiConsumer;

@Prototype
public class BsonEncoderBuilder extends AbstractBuilder implements Builder<Encoder> {
    private final Logger logger = LoggerFactory.getLogger(BsonEncoderBuilder.class);
    private final BsonMapper schemaMapper = new BsonMapper();

    @Override
    public Encoder build() {
        if (!schemaMapper.execute().succeeded()) {
            logger.error("Cannot make schema mapper, the encoder built is failed");
            return null;
        }
        Map<ReportType, ReportMapperAbstract<Document>> reportMappers = schemaMapper.getResult();
        BsonEncoder result = new BsonEncoder(reportMappers);
        BsonEncoderConfig config = this.convertAndValidate(BsonEncoderConfig.class);
        if (config.addMetaKey) {
            var metaKeyMaker = ReportKeyMaker.makeMetaKeyMaker();
            result.withMetaBuilder(metaKeyMaker);
        }
        var reportTypeMaker = this.makeReportTypeMetaKey();
        result.withMetaBuilder(reportTypeMaker);
        return result;
    }

    public static class BsonEncoderConfig {
        public boolean addMetaKey = true;
    }

    private BiConsumer<EncodedRecord.Builder, Report> makeReportTypeMetaKey() {
        return new BiConsumer<EncodedRecord.Builder, Report>() {
            @Override
            public void accept(EncodedRecord.Builder builder, Report report) {
                builder.withReportType(report.getType());
            }
        };
    }
}
