package org.observertc.observer.evaluators;

import io.micrometer.core.annotation.Timed;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.observer.samples.CollectedSfuSamples;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.observer.samples.SfuSamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

import static org.observertc.observer.micrometer.ExposedMetrics.OBSERVERTC_EVALUATORS_COLLECT_SFU_SAMPLES_TIME;

/**
 * Responsible to map the received client sample into an organized map of ClientSamples,
 * so the successor components can work by a batch of samples groupped by clients
 */
@Prototype
public class CollectSfuSamples implements Function<List<ObservedSfuSample>, Optional<CollectedSfuSamples>> {

    private static final Logger logger = LoggerFactory.getLogger(CollectSfuSamples.class);

    @PostConstruct
    void setup() {

    }


    @Override
    @Timed(value = OBSERVERTC_EVALUATORS_COLLECT_SFU_SAMPLES_TIME)
    public Optional<CollectedSfuSamples> apply(List<ObservedSfuSample> observedSfuSamples) throws Throwable {
        if (Objects.isNull(observedSfuSamples) || observedSfuSamples.size() < 1) {
            return Optional.empty();
        }
        Map<UUID, SfuSamples.Builder> builders = new HashMap<>();
        for (ObservedSfuSample observedSfuSample : observedSfuSamples) {
            UUID sfuId = observedSfuSample.getSfuId();
            SfuSamples.Builder builder = builders.get(sfuId);
            if (Objects.isNull(builder)) {
                builder = SfuSamples.builderFrom(observedSfuSample);
                builders.put(sfuId, builder);
            }
            builder.withObservedSfuSample(observedSfuSample);
        }
        try {
            CollectedSfuSamples.Builder collectSfuSamplesBuilder = CollectedSfuSamples.builder();
            builders.values().stream().map(SfuSamples.Builder::build).forEach(collectSfuSamplesBuilder::withSfuSamples);
            var result = collectSfuSamplesBuilder.build();
            return Optional.of(result);
        } catch (Exception ex) {
            logger.warn("Error occurred while collecting sfu samples", ex);
            return Optional.empty();
        }
    }
}
