package org.observertc.webrtc.observer.common;

import org.observertc.webrtc.schemas.reports.ReportType;

import java.util.*;
import java.util.stream.Stream;

public class OutboundReports implements Iterable<OutboundReport> {


    public static OutboundReports.Builder builder() {
        return new Builder();
    }

    public static OutboundReports fromList(List<OutboundReport> outboundReportList) {
        var builder = new Builder();
        outboundReportList.stream().forEach(builder::withOutboundReport);
        return builder.build();
    }

    private int reportsNum = 0;
    private Map<ReportType, List<OutboundReport>> outboundReports = new HashMap<>();

    private OutboundReports() {

    }

    public int getReportsNum() {
        return this.reportsNum;
    }

    @Override
    public Iterator<OutboundReport> iterator() {
        return this.outboundReports.values().stream().flatMap(List::stream).iterator();
    }

    public Stream<OutboundReport> stream() {
        return this.outboundReports.values().stream().flatMap(List::stream);
    }

    public Stream<OutboundReport> streamByType(ReportType type) {
        List<OutboundReport> outboundReports = this.outboundReports.get(type);
        if (Objects.isNull(outboundReports)) {
            return Stream.empty();
        }
        return outboundReports.stream();
    }

    public static class Builder {
        private final OutboundReports result = new OutboundReports();

        private Builder() {

        }

        public OutboundReports build() {
            return this.result;
        }

        public Builder withOutboundReport(OutboundReport outboundReport) {
            List<OutboundReport> outboundReports = this.result.outboundReports.get(outboundReport.getType());
            if (Objects.isNull(outboundReports)) {
                outboundReports = new LinkedList<>();
                this.result.outboundReports.put(outboundReport.getType(), outboundReports);
            }
            outboundReports.add(outboundReport);
            ++this.result.reportsNum;
            return this;
        }
    }
}

