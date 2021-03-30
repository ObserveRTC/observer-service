package org.observertc.webrtc.observer.connectors.encoders.avro;

import org.apache.avro.message.BinaryMessageEncoder;
import org.observertc.webrtc.observer.connectors.encoders.EncoderAbstract;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class AvroEncoder extends EncoderAbstract<Report> {
    private static final Logger logger = LoggerFactory.getLogger(AvroEncoder.class);
    private final BinaryMessageEncoder<Report> encoder;

    public AvroEncoder() {
        super();
        this.encoder = Report.getEncoder();
    }

    @Override
    protected Report make(Report report) throws Throwable {
        return report;
    }

    @Override
    protected byte[] convertToBytes(Report report) throws Throwable {
        ByteBuffer message = this.encoder.encode(report);
        return message.array();
    }

}
