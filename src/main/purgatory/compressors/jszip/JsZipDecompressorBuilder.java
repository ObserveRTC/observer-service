package org.observertc.webrtc.observer.compressors.jszip;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.compressors.Decompressor;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Prototype
public class JsZipDecompressorBuilder extends AbstractBuilder implements Builder<Decompressor> {

    private Map<String, Object> config = new HashMap<>();

    @Override
    public void withConfiguration(Map<String, Object> configs) {
        Objects.requireNonNull(this.config);
        this.config = configs;
    }

    @Override
    public Object getConfiguration(String key) {
        return this.config.get(key);
    }

    @Override
    public Decompressor build() {
        Config config = this.convertAndValidate(Config.class);
        JsZipDecompressor result = new JsZipDecompressor();
        return result;
    }

    public static class Config {

    }
}
