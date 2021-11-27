package org.observertc.webrtc.observer.codecs;

import org.observertc.webrtc.observer.configs.ObserverConfig;

import javax.inject.Singleton;

@Singleton
public class OutboundReportsCodec {


    private final Encoder encoder;
    private final Decoder decoder;

    OutboundReportsCodec( ObserverConfig.ReportsConfig config,
                               OutboundReportsAvroEncoder avroEncoder,
                               OutboundReportsAvroDecoder avroDecoder,
                               OutboundReportsJsonEncoder jsonEncoder,
                               OutboundReportsJsonDecoder jsonDecoder) {
        switch (config.format) {
            case AVRO:
                this.encoder = avroEncoder;
                this.decoder = avroDecoder;
                break;
            case JSON:
                this.encoder = jsonEncoder;
                this.decoder = jsonDecoder;
                break;
            default:
                throw new IllegalStateException("Unrecognized reportCodec {} " + config.format);
        }
    }

    public Encoder getEncoder() {
        return this.encoder;
    }

    public Decoder getDecoder() {
        return this.decoder;
    }
}
