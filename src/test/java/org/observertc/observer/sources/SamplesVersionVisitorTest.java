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
                VOID -> org.observertc.schemas.v200.samples.Samples.VERSION,
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
                () -> org.observertc.schemas.v200.samples.Samples.VERSION,
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
        var isV200Valid = SamplesVersionVisitor.isVersionValid("2.0.0");
        var isV200Beta65Valid = SamplesVersionVisitor.isVersionValid("2.0.0-beta.65");
        var isV200Beta64Valid = SamplesVersionVisitor.isVersionValid("2.0.0-beta.64");
        var isV200Beta63Valid = SamplesVersionVisitor.isVersionValid("2.0.0-beta.63");
        var isV200Beta62Valid = SamplesVersionVisitor.isVersionValid("2.0.0-beta.62");
        var isV200Beta61Valid = SamplesVersionVisitor.isVersionValid("2.0.0-beta.61");
        var isV200Beta60Valid = SamplesVersionVisitor.isVersionValid("2.0.0-beta.60");
        var isV200Beta59Valid = SamplesVersionVisitor.isVersionValid(org.observertc.schemas.v200beta59.samples.Samples.VERSION);
        var isRandomStringValid = SamplesVersionVisitor.isVersionValid(UUID.randomUUID().toString());
        Assertions.assertTrue(isLatestValid);
        Assertions.assertTrue(isV200Valid);
        Assertions.assertTrue(isV200Beta65Valid);
        Assertions.assertTrue(isV200Beta64Valid);
        Assertions.assertTrue(isV200Beta63Valid);
        Assertions.assertTrue(isV200Beta62Valid);
        Assertions.assertTrue(isV200Beta61Valid);
        Assertions.assertTrue(isV200Beta60Valid);
        Assertions.assertTrue(isV200Beta59Valid);
        Assertions.assertFalse(isRandomStringValid);
    }
}