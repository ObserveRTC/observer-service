package org.observertc.webrtc.observer.monitors;


import io.micrometer.core.instrument.Tag;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.ObservableOperator;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;
import java.util.function.BiConsumer;

@Prototype
public class CounterMonitorBuilder<T> extends AbstractBuilder implements Builder<ObservableOperator<T, T>> {

    @Inject
    Provider<CounterMonitor<T>> reportMonitorProvider;

    private BiConsumer<T, List<Tag>> tagsResolver;
    private String name;

    public ObservableOperator<T, T> build() {
        Config config = this.convertAndValidate(Config.class);
        CounterMonitor<T> result = this.reportMonitorProvider.get();
        Iterator<Map.Entry<String, String>> it = config.tagExtractors.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String tagName = entry.getKey();
            String extractorClass = entry.getValue();
            Optional<java.util.function.Function<T, String>> extractor = this.tryInvoke(extractorClass);
            if (!extractor.isPresent()) {
                continue;
            }
            this.withTagResolver(tagName, extractor.get());
        }
        return result.withTagsResolver(this.tagsResolver).withName(this.name);
    }

    CounterMonitorBuilder<T> withTagResolver(String tagName, java.util.function.Function<T, String> valueExtractor) {

        BiConsumer<T, List<Tag>> tagsResolver = (input, tags) -> {
            String tagValue = valueExtractor.apply(input);
            if (Objects.nonNull(tagValue)) {
                Tag tag = Tag.of(tagName, tagValue);
                tags.add(tag);
            }
        };
        if (Objects.isNull(this.tagsResolver)) {
            this.tagsResolver = tagsResolver;
        } else {
            this.tagsResolver = this.tagsResolver.andThen(tagsResolver);
        }

        return this;
    }

    CounterMonitorBuilder<T> withName(String value) {
        this.name = value;
        return this;
    }



    public static class Config {
        public String name = "unnamed";
        public Map<String, String> tagExtractors = new HashMap<>();
    }
}
