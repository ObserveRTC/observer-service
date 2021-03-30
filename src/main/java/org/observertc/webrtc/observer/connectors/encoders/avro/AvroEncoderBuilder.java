package org.observertc.webrtc.observer.connectors.encoders.avro;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.observertc.webrtc.observer.connectors.encoders.Encoder;
import org.observertc.webrtc.observer.connectors.encoders.ReportKeyMaker;

@Prototype
public class AvroEncoderBuilder extends AbstractBuilder implements Builder<Encoder> {

    @Override
    public Encoder build() {
        AvroEncoder result = new AvroEncoder();
        AvroEncoderConfig config = this.convertAndValidate(AvroEncoderConfig.class);
        if (config.addMetaKey) {
            var metaKeyMaker = ReportKeyMaker.makeMetaKeyMaker();
            result.withMetaBuilder(metaKeyMaker);
        }
        return result;
    }

    public static class AvroEncoderConfig {
        public boolean addMetaKey = true;
    }
}
