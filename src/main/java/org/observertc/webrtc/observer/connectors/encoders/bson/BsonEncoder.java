package org.observertc.webrtc.observer.connectors.encoders.bson;

import org.bson.Document;
import org.observertc.webrtc.observer.connectors.encoders.EncoderAbstract;
import org.observertc.webrtc.observer.connectors.encoders.ReportMapperAbstract;
import org.observertc.webrtc.observer.connectors.encoders.avro.AvroEncoder;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class BsonEncoder extends EncoderAbstract<Document> {

    private static final Logger logger = LoggerFactory.getLogger(AvroEncoder.class);
    private final Map<ReportType, ReportMapperAbstract<Document>> reportMappers;

    public BsonEncoder(Map<ReportType, ReportMapperAbstract<Document>> reportMappers) {
        this.reportMappers = reportMappers;
    }

    @Override
    protected Document make(Report report) {
        ReportMapperAbstract<Document> reportMapper = this.reportMappers.get(report.getType());
        if (Objects.isNull(reportMapper)) {
            logger.warn("No Report Mapper found for report type {}", report.getType());
            return null;
        }
        Document result = reportMapper.apply(report);
        return result;
    }

    @Override
    protected byte[] convertToBytes(Document document) throws Throwable {
        byte[] result = document.toJson().getBytes(StandardCharsets.UTF_8);
        return result;
    }

}
