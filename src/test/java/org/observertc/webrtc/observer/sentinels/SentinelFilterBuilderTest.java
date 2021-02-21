package org.observertc.webrtc.observer.sentinels;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class SentinelFilterBuilderTest {

    @Inject
    Provider<SentinelFilterBuilder> sentinelFilterBuilderProvider;


}