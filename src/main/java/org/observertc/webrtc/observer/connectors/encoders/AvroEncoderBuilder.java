package org.observertc.webrtc.observer.connectors.encoders;

import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.observertc.webrtc.schemas.reports.Report;

import java.util.UUID;
import java.util.function.BiConsumer;

public class AvroEncoderBuilder extends AbstractBuilder implements Builder<Encoder> {

    @Override
    public Encoder build() {
        AvroEncoder result = new AvroEncoder();
        AvroEncoderConfig config = this.convertAndValidate(AvroEncoderConfig.class);
        if (config.addMetaKey) {
            BiConsumer<EncodedRecord.Builder, Report> metaKeyBuilder = this.makeMetaKeyMaker();
            result.withMetaBuilder(metaKeyBuilder);
        }
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

    public static class AvroEncoderConfig {
        public boolean addMetaKey = true;
    }
}
