package org.observertc.webrtc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;

@MicronautTest
class ExpiredPCsEvaluatorTest {

    @Inject
    Provider<ExpiredPCsEvaluator> subject;


    @Test
    public void shouldRemovePeerConnection() throws Throwable {
    }

    @Test
    public void shouldRemoveCall() throws Throwable {
    }
}