package org.observertc.observer.mappings;

import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.Function;

public interface Codec<TInput, TOutput> extends Encoder<TInput, TOutput>, Decoder<TOutput, TInput> {
    static<TInput, TOutput> Codec<TInput, TOutput> create(Function<TInput, TOutput> encoder, Function<TOutput, TInput> decoder, Logger _logger) {
        return new Codec<TInput, TOutput>() {
            @Override
            public TOutput encode(TInput data) {
                try {
                    return encoder.apply(data);
                } catch (Exception ex) {
                    _logger.warn("Error occurred while encoding", ex);
                    return null;
                }
            }
            @Override
            public TInput decode(TOutput data) {
                try {
                    return decoder.apply(data);
                } catch (Exception ex) {
                    _logger.warn("Error occurred while decoding", ex);
                    return null;
                }
            }
        };
    }

    static<TInput, TOutput> Codec<TInput, TOutput> create(Mapper<TInput, TOutput> encoder, Mapper<TOutput, TInput> decoder) {
        return new Codec<TInput, TOutput>() {
            @Override
            public TOutput encode(TInput data) {
                return encoder.map(data);
            }

            @Override
            public TInput decode(TOutput data) {
                return decoder.map(data);
            }
        };
    }

    static <TInput, TMiddle, TOutput> Codec<TInput, TOutput> link(Codec<TInput, TMiddle> prev, Codec<TMiddle, TOutput> next) {
        Objects.requireNonNull(prev);
        Objects.requireNonNull(next);
        var result = new Codec<TInput, TOutput>() {
            @Override
            public TOutput encode(TInput data) {
                var middle = prev.encode(data);
                return next.encode(middle);
            }

            @Override
            public TInput decode(TOutput data) {
                var middle = next.decode(data);
                return prev.decode(middle);
            }
        };
        return result;
    }
}
