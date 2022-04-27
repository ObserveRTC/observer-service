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
                VOID -> org.observertc.schemas.v200beta64.samples.Samples.VERSION,
                VOID -> org.observertc.schemas.v200beta59.samples.Samples.VERSION,
                VOID -> notRecognized
        );
        Assertions.assertEquals(Samples.VERSION, visitor.apply(null, Samples.VERSION));
        Assertions.assertEquals(org.observertc.schemas.v200beta59.samples.Samples.VERSION, visitor.apply(null, org.observertc.schemas.v200beta59.samples.Samples.VERSION));
        Assertions.assertEquals(notRecognized, visitor.apply(null, UUID.randomUUID().toString()));

    }

    @Test
    void shouldBeCorrectCallInvocationsForSuppliers() {
        var notRecognized = "notRecognized";
        var visitor = SamplesVersionVisitor.<String>createSupplierVisitor(
                () -> Samples.VERSION,
                () -> org.observertc.schemas.v200beta64.samples.Samples.VERSION,
                () -> org.observertc.schemas.v200beta59.samples.Samples.VERSION,
                () -> notRecognized
        );
        Assertions.assertEquals(Samples.VERSION, visitor.apply(null, Samples.VERSION));
        Assertions.assertEquals(org.observertc.schemas.v200beta59.samples.Samples.VERSION, visitor.apply(null, org.observertc.schemas.v200beta59.samples.Samples.VERSION));
        Assertions.assertEquals(notRecognized, visitor.apply(null, UUID.randomUUID().toString()));
    }


    @Test
    void shouldValidateVersions() {
        var isLatestValid = SamplesVersionVisitor.isVersionValid(Samples.VERSION);
        var isV200Beta59Valid = SamplesVersionVisitor.isVersionValid(org.observertc.schemas.v200beta59.samples.Samples.VERSION);
        var isRandomStringValid = SamplesVersionVisitor.isVersionValid(UUID.randomUUID().toString());
        Assertions.assertTrue(isLatestValid);
        Assertions.assertTrue(isV200Beta59Valid);
        Assertions.assertFalse(isRandomStringValid);
    }
}