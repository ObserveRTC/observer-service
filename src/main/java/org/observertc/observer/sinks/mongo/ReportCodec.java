package org.observertc.observer.sinks.mongo;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.observertc.observer.reports.Report;

public class ReportCodec implements Codec<Report> {
    @Override
    public Report decode(BsonReader reader, DecoderContext decoderContext) {
        return null;
    }

    @Override
    public void encode(BsonWriter writer, Report value, EncoderContext encoderContext) {

    }

    @Override
    public Class<Report> getEncoderClass() {
        return null;
    }
}
