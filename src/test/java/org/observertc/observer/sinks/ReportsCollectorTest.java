package org.observertc.observer.sinks;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.reports.Report;
import org.observertc.observer.utils.ReportGenerators;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest(environments = "test")
class ReportsCollectorTest {

    @Inject
    ReportsCollector reportsCollector;

    ReportGenerators generator = new ReportGenerators();

    @Test
    void collectAndEmit() throws ExecutionException, InterruptedException, TimeoutException {
        var expected = generator.generateReport();
        var emitted = new CompletableFuture<List<Report>>();
        reportsCollector.getObservableReports().subscribe(emitted::complete);
        reportsCollector.accept(expected);
        reportsCollector.teardown();
        var actual = emitted.get(60, TimeUnit.SECONDS).get(0);
        Assertions.assertEquals(expected, actual);
    }

}