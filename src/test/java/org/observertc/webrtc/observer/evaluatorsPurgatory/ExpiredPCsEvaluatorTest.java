package org.observertc.webrtc.observer.evaluatorsPurgatory;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.observertc.webrtc.observer.evaluatorsPurgatory.pcSampleToReportsV2.ExpiredPCsEvaluator;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class ExpiredPCsEvaluatorTest {

    @Inject
    Provider<ExpiredPCsEvaluator> subject;

}