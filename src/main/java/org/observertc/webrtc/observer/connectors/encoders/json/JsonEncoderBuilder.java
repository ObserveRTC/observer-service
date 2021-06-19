package org.observertc.webrtc.observer.connectors.encoders.json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.observertc.webrtc.observer.connectors.encoders.Encoder;
import org.observertc.webrtc.observer.connectors.encoders.ReportKeyMaker;
import org.observertc.webrtc.observer.connectors.encoders.ReportMapperAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Prototype
public class JsonEncoderBuilder extends AbstractBuilder implements Builder<Encoder> {
    private final Logger logger = LoggerFactory.getLogger(JsonEncoderBuilder.class);
    private final JsonMapper schemaMapper = new JsonMapper();

    @Override
    public Encoder build() {
        if (!schemaMapper.execute().succeeded()) {
            logger.error("Cannot make schema mapper, the encoder built is failed");
            return null;
        }
//        Map<ReportType, ReportMapperAbstract<JsonObject>> reportMappers = schemaMapper.getResult();
        Map<ReportType, ReportMapperAbstract<ObjectNode>> reportMappers = schemaMapper.getResult();
        JsonEncoder result = new JsonEncoder(reportMappers);
        JacksonEncoderConfig config = this.convertAndValidate(JacksonEncoderConfig.class);
        if (config.addMetaKey) {
            var metaKeyMaker = ReportKeyMaker.makeMetaKeyMaker();
            result.withMetaBuilder(metaKeyMaker);
        }
        return result;
    }

    public static class JacksonEncoderConfig {
        public boolean addMetaKey = true;
    }
}
