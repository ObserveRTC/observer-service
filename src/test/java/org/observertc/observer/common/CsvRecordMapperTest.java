package org.observertc.observer.common;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.reports.Report;
import org.observertc.observer.utils.ReportGenerators;
import org.observertc.schemas.reports.csvsupport.ClientExtensionReportToIterable;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

class CsvRecordMapperTest {

    @Test
    void shouldBeValid_1() throws IOException {
        var report = Report.fromClientExtensionReport(new ReportGenerators().generateClientExtensionReport());
        var written = new ClientExtensionReportToIterable().apply(report);

        var tempFileName = writeToTempFile(written);

        var read = this.readRecord(tempFileName);
        var equals = this.equals(written, read);
        Assertions.assertTrue(equals);
    }

    @Test
    void shouldBeValid_2() throws IOException {
        var clientExtensionReport = new ReportGenerators().generateClientExtensionReport();
        clientExtensionReport.payload = JsonUtils.objectToString(new ReportGenerators().generateCallEventReport());
        var report = Report.fromClientExtensionReport(clientExtensionReport);
        var written = new ClientExtensionReportToIterable().apply(report);

        var tempFileName = writeToTempFile(written);

        var read = this.readRecord(tempFileName);
        var equals = this.equals(written, read);
        Assertions.assertTrue(equals);
    }

    private String writeToTempFile(Iterable<?> source) throws IOException {
        var mapper = CsvRecordMapper.builder().build();
        var line = mapper.apply(source);
        Path tempCsv = Files.createTempFile(null, ".csv");
        FileWriter myWriter = new FileWriter(tempCsv.toString());

        myWriter.write(line);
        myWriter.close();
        return tempCsv.toString();
    }

    private CSVRecord readRecord(String tempFileName) throws IOException {
        Reader in = new FileReader(tempFileName);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
        return records.iterator().next();
    }

    private boolean equals(Iterable<?> source, CSVRecord record) {
        var sourceIt = source.iterator();
        int i;
        for (i = 0; sourceIt.hasNext(); ++i) {
            var item = sourceIt.next();
            var written = item.toString();
            var read = record.get(i);
//            System.out.println(read + " <-> " + written);
            if (!written.equals(read)) {
                return false;
            }
        }
        return i == record.size();
    }
}