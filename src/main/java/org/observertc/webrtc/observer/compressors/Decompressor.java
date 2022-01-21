package org.observertc.webrtc.observer.compressors;


import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Function;

public interface Decompressor extends Function<byte[], byte[]> {
    static Decompressor of(Function<byte[], byte[]> transformer, Action closer) {
        return new Decompressor() {
            @Override
            public void close() throws Throwable {
                closer.run();
            }

            @Override
            public byte[] apply(byte[] bytes) throws Throwable {
                return transformer.apply(bytes);
            }
        };
    }

    void close() throws Throwable;
}
