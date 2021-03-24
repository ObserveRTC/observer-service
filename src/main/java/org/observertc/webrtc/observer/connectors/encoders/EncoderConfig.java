package org.observertc.webrtc.observer.connectors.encoders;

import java.util.Map;

public class EncoderConfig {

    public String type = AvroEncoder.class.getSimpleName();

    public Map<String, Object> config;

}
