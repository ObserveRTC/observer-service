package org.observertc.webrtc.observer.connectors.encoders.json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.observertc.webrtc.observer.connectors.encoders.EncoderAbstract;
import org.observertc.webrtc.observer.connectors.encoders.ReportMapperAbstract;
import org.observertc.webrtc.observer.connectors.encoders.avro.AvroEncoder;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class JsonEncoder extends EncoderAbstract<ObjectNode> {

    private static final Logger logger = LoggerFactory.getLogger(AvroEncoder.class);
    private final Map<ReportType, ReportMapperAbstract<ObjectNode>> reportMappers;

    public JsonEncoder(Map<ReportType, ReportMapperAbstract<ObjectNode>> reportMappers) {
        this.reportMappers = reportMappers;
    }

    @Override
    protected ObjectNode make(Report report) throws Throwable{
        ReportMapperAbstract<ObjectNode> reportMapper = this.reportMappers.get(report.getType());
        if (Objects.isNull(reportMapper)) {
            logger.warn("No Report Mapper found for report type {}", report.getType());
            return null;
        }
        return reportMapper.apply(report);
    }

    @Override
    protected byte[] convertToBytes(ObjectNode object) throws IOException {
        return object.toString().getBytes();
    }
}
