package org.observertc.observer.codecs;

import org.bson.internal.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public interface Codec<U, R> {
    String BASE_64_CODEC_LOGGER = "Base64Codec";
    String STRING_TO_BYTES_CODEC_LOGGER = "StringToBytesCodec";
    String DEFAULT_LOGGER = "DefaultLogger";
    Map<String, Logger> loggers = Map.of(
            BASE_64_CODEC_LOGGER, LoggerFactory.getLogger(BASE_64_CODEC_LOGGER),
            STRING_TO_BYTES_CODEC_LOGGER, LoggerFactory.getLogger(STRING_TO_BYTES_CODEC_LOGGER),
            DEFAULT_LOGGER, LoggerFactory.getLogger(Codec.class)
    );

    static<T, U> Codec<T, U> create(Function<T, U> encoder, Function<U, T> decoder, Logger _logger) {
        return new Codec<T, U>() {
            @Override
            public U encode(T data) {
                try {
                    return encoder.apply(data);
                } catch (Exception ex) {
                    _logger.warn("Error occurred while encoding", ex);
                    return null;
                }
            }
            @Override
            public T decode(U data) {
                try {
                    return decoder.apply(data);
                } catch (Exception ex) {
                    _logger.warn("Error occurred while decoding", ex);
                    return null;
                }
            }
        };
    }
    static<T, U> Codec<T, U> create(Function<T, U> encoder, Function<U, T> decoder) {
        var logger = loggers.get(DEFAULT_LOGGER);
        return create(encoder, decoder, logger);
    }

    static Codec<byte[], String> createBase64Codec() {
        Logger logger = loggers.get(BASE_64_CODEC_LOGGER);
        return create(Base64::encode, Base64::decode, logger);
    }

    static Codec<String, byte[]> createStringToBytesCodec() {
        Logger logger = loggers.get(STRING_TO_BYTES_CODEC_LOGGER);
        return create(String::getBytes, String::new, logger);
    }

    static Codec<byte[], String> createBytesToStringCodec() {
        Logger logger = loggers.get(STRING_TO_BYTES_CODEC_LOGGER);
        return create(String::new, String::getBytes, logger);
    }

    R encode(U data);
    U decode(R data);

    default <V> Codec<U, V> append(Codec<R, V> nextCodec) {
        Objects.requireNonNull(nextCodec);
        Codec<U, R> prevCodec = this;
        Codec<U, V> result = new Codec<U, V>() {
            @Override
            public V encode(U data) {
                var middle = prevCodec.encode(data);
                return nextCodec.encode(middle);
            }

            @Override
            public U decode(V data) {
                var middle = nextCodec.decode(data);
                return prevCodec.decode(middle);
            }
        };
        return result;
    }

    default <V> Codec<V, R> prepend(Codec<V, U> prevCodec) {
        Objects.requireNonNull(prevCodec);
        Codec<U, R> nextCodec = this;
        Codec<V, R> result = new Codec<V, R>() {
            @Override
            public R encode(V data) {
                var middle = prevCodec.encode(data);
                return nextCodec.encode(middle);
            }

            @Override
            public V decode(R data) {
                var middle = nextCodec.decode(data);
                return prevCodec.decode(middle);
            }
        };
        return result;
    }
}
