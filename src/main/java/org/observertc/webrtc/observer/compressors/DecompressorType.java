package org.observertc.webrtc.observer.compressors;

public enum DecompressorType {
    NONE("None"),
    JSZIP("JsZipDecompressor");


    private String klassName;

    DecompressorType(String klassName) {
        this.klassName = klassName;
    }

    public String getKlassName() {
        return this.klassName;
    }
}
