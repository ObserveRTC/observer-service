package org.observertc.webrtc.observer.sentinels;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;
import org.observertc.webrtc.observer.dto.CollectionFilterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Prototype
public class CollectionFilterBuilder {
    private static final Logger logger = LoggerFactory.getLogger(CollectionFilterBuilder.class);

    public<T> Predicate<Collection<T>> build(CollectionFilterDTO config, Function<String, T> converter) {
        List<T> allMatches = this.fetchItems(config.allMatch, converter);
        List<T> anyMatches = this.fetchItems(config.anyMatch, converter);
        return new Predicate<Collection<T>>() {
            @Override
            public boolean test(Collection<T> values) throws Throwable {
                if (Objects.isNull(values)) {
                    return false;
                }
                int count = values.size();
                if (config.eq != -1 && config.eq == count) {
                    return true;
                }
                if (config.gt != -1 && config.lt != -1) {
                    if (config.gt < count && count < config.lt) {
                        return true;
                    }
                } else if (config.lt != -1 && count < config.lt) {
                    return true;
                } else if (config.gt != -1 && config.gt < count) {
                    return true;
                }

                if (Objects.nonNull(config.allMatch) && 0 < allMatches.size()) {
                    if (allMatches.stream().allMatch(item -> values.contains(item))) {
                        return true;
                    }
                }

                if (Objects.nonNull(config.anyMatch) && 0 < anyMatches.size()) {
                    if (anyMatches.stream().anyMatch(item -> values.contains(item))) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private<T> List<T> fetchItems(String[] items, Function<String, T> converter) {
        List<T> result = new LinkedList<>();
        for (String item : items) {
            try {
                T value = converter.apply(item);
                if (Objects.isNull(value)) {
                    logger.warn("Converting {} resulted null. It will not be added to the fetched values of the filtered values", item);
                    continue;
                }
                result.add(value);
            } catch (Throwable throwable) {
                logger.warn("Cannot convert value {}", item, throwable);
            }
        }
        return result;
    }

//    public<T> Predicate<CallEntity> build(CollectionFilterDTO config, Function<CallEntity, Collection<T>> extractor, Function<String, T> converter) {
//        List<T> allMatches = new LinkedList<>();
//        for (String item : config.allMatch) {
//            try {
//                T value = converter.apply(item);
//                allMatches.add(value);
//            } catch (Throwable throwable) {
//                logger.warn("Cannot convert value {}", item, throwable);
//            }
//        }
//
//        List<T> anyMatches = new LinkedList<>();
//        for (String item : config.anyMatch) {
//            try {
//                T value = converter.apply(item);
//                anyMatches.add(value);
//            } catch (Throwable throwable) {
//                logger.warn("Cannot convert value {}", item, throwable);
//            }
//        }
//        return new Predicate<CallEntity>() {
//            @Override
//            public boolean test(CallEntity callEntity) throws Throwable {
//                Collection<T> values = extractor.apply(callEntity);
//                if (Objects.isNull(values)) {
//                    return false;
//                }
//                int count = values.size();
//                if (config.eq != -1 && config.eq == count) {
//                    return true;
//                }
//                if (config.gt != -1 && config.lt != -1) {
//                    if (config.gt < count && count < config.lt) {
//                        return true;
//                    }
//                } else if (config.lt != -1 && count < config.lt) {
//                    return true;
//                } else if (config.gt != -1 && config.gt < count) {
//                    return true;
//                }
//
//                if (Objects.nonNull(config.allMatch)) {
//                    if (allMatches.stream().allMatch(item -> values.contains(item))) {
//                        return true;
//                    }
//                }
//
//                if (Objects.nonNull(config.anyMatch)) {
//                    if (anyMatches.stream().anyMatch(item -> values.contains(item))) {
//                        return true;
//                    }
//                }
//                return false;
//            }
//        };
//    }
}
