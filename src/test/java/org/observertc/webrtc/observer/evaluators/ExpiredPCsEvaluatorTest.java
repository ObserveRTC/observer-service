package org.observertc.webrtc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class ExpiredPCsEvaluatorTest {

    @Inject
    Provider<ExpiredPCsEvaluator> subject;

}