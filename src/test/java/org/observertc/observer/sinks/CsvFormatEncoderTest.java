package org.observertc.observer.sinks;

import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportType;
import org.observertc.observer.utils.ReportGenerators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

class CsvFormatEncoderTest {

    private static final Logger logger = LoggerFactory.getLogger(CsvFormatEncoderTest.class);

    private ReportGenerators reportGenerators = new ReportGenerators();

    @Test
    @DisplayName("When reports are added and mapped by type")
    void test_1() {
        var formatEncoder = new CsvFormatEncoder<ReportType, String>(
                2,
                report -> report.type,
                Function.identity(),
                CSVFormat.DEFAULT,
                logger
        );
        var reports = List.of(
                Report.fromCallEventReport(this.reportGenerators.generateCallEventReport()),
                Report.fromCallMetaReport(this.reportGenerators.generateCallMetaReport()),
                Report.fromCallEventReport(this.reportGenerators.generateCallEventReport()),
                Report.fromCallEventReport(this.reportGenerators.generateCallEventReport())
        );

        var map = formatEncoder.map(reports);
        Assertions.assertEquals(2, map.get(ReportType.CALL_EVENT).size());
        Assertions.assertEquals(1, map.get(ReportType.CALL_META_DATA).size());

    }
}