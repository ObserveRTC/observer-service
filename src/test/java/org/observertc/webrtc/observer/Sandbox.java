package org.observertc.webrtc.observer;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.jeasy.random.api.Randomizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.samples.ClientSampleGenerator;

import javax.inject.Inject;
import java.util.Map;

@MicronautTest
public class Sandbox {

    @Inject
    ReportGenerators reportGenerators;

    @Inject
    ClientSampleGenerator clientSampleGenerator;

    @Test
    public void get() {
        var report = this.reportGenerators.getCallMetaReport();
        Assertions.assertTrue(true);
    }

    Map<String, Randomizer> fields = Map.of(
            "foo", () -> 1,
            "bar", () -> "str"
    );

    @Test
    public void t() {
        var r = this.clientSampleGenerator.get();
        return;
    }

    public class A {
        public int foo;
        public String bar;
    }
}
