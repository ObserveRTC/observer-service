package org.observertc.webrtc.observer.connectors.encoders;

import org.observertc.webrtc.observer.connectors.encoders.avro.AvroEncoder;

import java.util.Map;

public class EncoderConfig {

    public String type = AvroEncoder.class.getSimpleName();

    public Map<String, Object> config;

}
