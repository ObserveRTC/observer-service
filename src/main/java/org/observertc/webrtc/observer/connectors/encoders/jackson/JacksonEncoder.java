package org.observertc.webrtc.observer.connectors.encoders.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.observertc.webrtc.observer.connectors.MessageFormat;
import org.observertc.webrtc.observer.connectors.encoders.Encoder;
import org.observertc.webrtc.observer.connectors.encoders.ReportMapper;
import org.observertc.webrtc.observer.connectors.encoders.avro.AvroEncoder;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class JacksonEncoder implements Encoder {

    private static final Logger logger = LoggerFactory.getLogger(AvroEncoder.class);
    private final ObjectMapper objectMapper;
    private final Map<ReportType, ReportMapper> reportMappers;
    private BiConsumer<EncodedRecord.Builder, Report> metaBuilder = (e, r) -> {};

    public JacksonEncoder(Map<ReportType, ReportMapper> reportMappers) {
        this.objectMapper = new ObjectMapper();
        this.reportMappers = reportMappers;
    }

    @Override
    public EncodedRecord apply(Report report) throws Throwable {
        ReportMapper reportMapper = this.reportMappers.get(report.getType());
        if (Objects.isNull(reportMapper)) {
            logger.warn("No Report Mapper found for report type {}", report.getType());
            return null;
        }
        Map<String, Object> map;
        byte[] message;
        try {
            map = reportMapper.apply(report);
            message = this.objectMapper.writeValueAsBytes(map);
        } catch (Throwable t) {
            logger.error("An error occurred during conversion");
            return null;
        }

        EncodedRecord.Builder recordBuilder = EncodedRecord.builder();
        recordBuilder
                .withMessage(message)
                .withFormat(MessageFormat.JSON);;
        this.metaBuilder.accept(recordBuilder, report);
        return recordBuilder.build();
    }

    JacksonEncoder withMetaBuilder(BiConsumer<EncodedRecord.Builder, Report> metaBuilder) {
        this.metaBuilder = this.metaBuilder.andThen(metaBuilder);
        return this;
    }
}
