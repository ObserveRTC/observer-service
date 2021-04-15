package org.observertc.webrtc.observer.sentinels;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.Utils;
import org.observertc.webrtc.observer.configs.CollectionFilterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Prototype
public class CollectionFilterBuilder {
    private static final Logger logger = LoggerFactory.getLogger(CollectionFilterBuilder.class);

    public static boolean isEmpty(CollectionFilterConfig config) {
        return !hasSizeConstrain(config) &&
                Objects.nonNull(config.anyMatch) &&
                config.anyMatch.length < 1 &&
                Objects.nonNull(config.allMatch) &&
                config.allMatch.length < 1
                ;
    }

    private static boolean hasSizeConstrain(CollectionFilterConfig config) {
        return config.eq != -1 || config.lt != -1 || config.gt != -1;
    }

    private boolean warned = false;

    public<T> Predicate<Collection<T>> build(CollectionFilterConfig config, Function<String, T> converter) {
        if (Utils.anyNull(config, config.anyMatch, config.allMatch)) {
            logger.warn("One of the configuration for collection filter is null. It cannot be built, will always report false! The collection filter config is {}",
                    ObjectToString.toString(config));
            this.warned = true;
            return values -> false;
        }

        List<T> allMatches = this.fetchItems(config.allMatch, converter);
        List<T> anyMatches = this.fetchItems(config.anyMatch, converter);
        boolean performSizeConstrainCheck = this.hasSizeConstrain(config);
        boolean performAnyMatchCheck = 0 < anyMatches.size();
        boolean performAllMatchCheck = 0 < allMatches.size();
        if (!performSizeConstrainCheck && !performAnyMatchCheck && !performAllMatchCheck) {
            logger.warn("Collection filter {} does not have any constrain. it will always be true", ObjectToString.toString(config));
            this.warned = true;
            return values -> true;
        }
        final Predicate<Collection<T>> sizeConstrainPredicate;
        if (performSizeConstrainCheck) {
            sizeConstrainPredicate = this.buildSizeConstrain(config);
        } else {
            sizeConstrainPredicate = values -> true;
        }


        final Predicate<Collection<T>> anyMatchPredicate;
        if (performAnyMatchCheck) {
            anyMatchPredicate = values -> anyMatches.stream().anyMatch(item -> values.contains(item));
        } else {
            anyMatchPredicate = values -> true;
        }
        final Predicate<Collection<T>> allMatchPredicate;
        if (performAllMatchCheck) {
            allMatchPredicate = values -> allMatches.stream().allMatch(item -> values.contains(item));
        } else {
            allMatchPredicate = values -> true;
        }

        return new Predicate<Collection<T>>() {
            @Override
            public boolean test(Collection<T> values) throws Throwable {
                if (Objects.isNull(values)) {
                    logger.warn("Null values are provided to check a collection. it will return false");
                    return false;
                }

                boolean result = true;
                result &= sizeConstrainPredicate.test(values);
                result &= anyMatchPredicate.test(values);
                result &= allMatchPredicate.test(values);
                return result;
            }
        };
    }

    public boolean isWarned() {
        return this.warned;
    }

    private<T> Predicate<Collection<T>> buildSizeConstrain(CollectionFilterConfig config) {
        return values -> {
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
            return false;
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
}
