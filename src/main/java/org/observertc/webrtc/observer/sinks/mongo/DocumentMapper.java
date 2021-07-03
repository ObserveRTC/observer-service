package org.observertc.webrtc.observer.sinks.mongo;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.bson.Document;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.common.OutboundReportTypeVisitors;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

class DocumentMapper implements Function<OutboundReport, Document> {
    private static final Logger logger = LoggerFactory.getLogger(DocumentMapper.class);

    static Map<ReportType, DocumentMapper> getDocumentMappers() {
        Map<ReportType, DocumentMapper> result = new HashMap<>();
        var decoderProvider = OutboundReportTypeVisitors.decoderProvider();
        var schemaProvider = OutboundReportTypeVisitors.avroSchemaResolver();
        Arrays.stream(ReportType.values()).forEach(reportType -> {
            Schema schema = schemaProvider.apply(null, reportType);
            if (Objects.isNull(schema)) {
                logger.error("Cannot resolve avro schema for report type {}", reportType);
                return;
            }
            var decoder = decoderProvider.apply(null, reportType);
            if (Objects.isNull(decoder)) {
                logger.error("Cannot resolve decoder for report type {}", reportType);
                return;
            }
            var documentMapper = builder()
                    .withDecoder(decoder)
                    .forSchema(schema)
                    .build();
            result.put(reportType, documentMapper);
        });
        return result;
    }

    static Builder builder() {
        return new Builder();
    }
    private Map<String, Integer> fields;
    private Function<OutboundReport, SpecificRecordBase> decoder;

    private DocumentMapper() {

    }

    @Override
    public Document apply(OutboundReport outboundReport) {
        SpecificRecordBase record = this.decoder.apply(outboundReport);
        Document document = new Document();
        this.fields.forEach((fieldName, fieldPos) -> {
            try {
                var value = record.get(fieldPos);
                document.put(fieldName, value);
            } catch (Exception ex) {
                logger.warn("Cannot map field {} for avro schema", fieldName, ex);
            }
        });
        return document;
    }

    static class Builder {
        private Builder() {

        }

        private final DocumentMapper result = new DocumentMapper();

        public Builder withDecoder(Function<OutboundReport, SpecificRecordBase> decoder) {
            this.result.decoder = decoder;
            return this;
        }

        public Builder forSchema(Schema schema) {
            this.result.fields = schema.getFields().stream()
                    .collect(Collectors.toMap(
                            field -> field.name(),
                            field -> field.pos()
                    ));
            return this;
        }

        public DocumentMapper build() {
            Objects.requireNonNull(this.result.decoder);
            Objects.requireNonNull(this.result.fields);
            return this.result;
        }
    }
}
