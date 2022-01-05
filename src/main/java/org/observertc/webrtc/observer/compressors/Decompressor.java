package org.observertc.webrtc.observer.compressors;


import io.reactivex.rxjava3.functions.Function;

public interface Decompressor extends Function<byte[], byte[]> {

}
