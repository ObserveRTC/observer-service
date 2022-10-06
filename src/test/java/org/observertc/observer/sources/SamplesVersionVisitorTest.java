package org.observertc.observer.sources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.schemas.samples.Samples;

import java.util.UUID;

class SamplesVersionVisitorTest {

    @Test
    void shouldBeCorrectCallInvocationsForFunctions() {
        var notRecognized = "notRecognized";
        var visitor = SamplesVersionVisitor.<Void, String>createFunctionalVisitor(
                VOID -> Samples.VERSION,
                VOID -> notRecognized
        );
        Assertions.assertEquals(Samples.VERSION, visitor.apply(null, Samples.VERSION));
        Assertions.assertEquals(notRecognized, visitor.apply(null, UUID.randomUUID().toString()));

    }


    @Test
    void shouldValidateVersions() {
        var isLatestValid = SamplesVersionVisitor.isVersionValid(Samples.VERSION);
        var isV210Valid = SamplesVersionVisitor.isVersionValid("2.1.0");
        var isRandomStringValid = SamplesVersionVisitor.isVersionValid(UUID.randomUUID().toString());
        Assertions.assertTrue(isLatestValid);
        Assertions.assertTrue(isV210Valid);
        Assertions.assertFalse(isRandomStringValid);
    }
}