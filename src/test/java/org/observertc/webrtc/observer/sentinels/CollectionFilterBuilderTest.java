package org.observertc.webrtc.observer.sentinels;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.functions.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.configs.CollectionFilterConfig;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collection;
import java.util.List;

@MicronautTest
class CollectionFilterBuilderTest {

    @Inject
    Provider<CollectionFilterBuilder> collectionFilterBuilderProvider;

    @Test
    void shouldFilterCollectionsEqualTo_1() throws Throwable {
        CollectionFilterConfig collectionFilterConfig = CollectionFilterConfig.builder()
                .numOfElementsIsEqualTo(2)
                .build();

        Predicate<Collection<Integer>> filter = collectionFilterBuilderProvider.get().build(collectionFilterConfig, Integer::parseInt);

        Assertions.assertTrue(filter.test(List.of(1,2)));
        Assertions.assertFalse(filter.test(List.of(1,2,3)));
    }

    @Test
    void shouldFilterCollectionsLessThan_1() throws Throwable {
        CollectionFilterConfig collectionFilterConfig = CollectionFilterConfig.builder()
                .numOfElementsIsLessThan(2)
                .build();

        Predicate<Collection<Integer>> filter = collectionFilterBuilderProvider.get().build(collectionFilterConfig, Integer::parseInt);

        Assertions.assertTrue(filter.test(List.of(1)));
        Assertions.assertFalse(filter.test(List.of(1,2)));
    }

    @Test
    void shouldFilterCollectionsGreaterThan_1() throws Throwable {
        CollectionFilterConfig collectionFilterConfig = CollectionFilterConfig.builder()
                .numOfElementsIsGreaterThan(2)
                .build();

        Predicate<Collection<Integer>> filter = collectionFilterBuilderProvider.get().build(collectionFilterConfig, Integer::parseInt);

        Assertions.assertTrue(filter.test(List.of(1,2,3)));
        Assertions.assertFalse(filter.test(List.of(1,2)));
    }

    @Test
    void shouldFilterCollectionsHasAnyItems_1() throws Throwable {
        CollectionFilterConfig collectionFilterConfig = CollectionFilterConfig.builder()
                .anyOfTheElementsAreMatchingTo("2")
                .build();

        Predicate<Collection<Integer>> filter = collectionFilterBuilderProvider.get().build(collectionFilterConfig, Integer::parseInt);

        Assertions.assertTrue(filter.test(List.of(1,2,3)));
        Assertions.assertFalse(filter.test(List.of(1,3)));
    }

    @Test
    void shouldFilterCollectionsHasAllItems_1() throws Throwable {
        CollectionFilterConfig collectionFilterConfig = CollectionFilterConfig.builder()
                .allOfTheElementsAreMatchingTo("1","2")
                .build();
        Predicate<Collection<Integer>> filter = collectionFilterBuilderProvider.get().build(collectionFilterConfig, Integer::parseInt);

        Assertions.assertTrue(filter.test(List.of(1,2,3)));
        Assertions.assertFalse(filter.test(List.of(1)));
    }

}