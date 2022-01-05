package org.observertc.webrtc.observer.compressors.jszip;

import org.observertc.webrtc.observer.compressors.Decompressor;

public class JsZipDecompressor implements Decompressor {

    @Override
    public byte[] apply(byte[] bytes) {
        return new byte[0];
    }

    JsZipDecompressor() {

    }
}
