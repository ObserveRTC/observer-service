package org.observertc.webrtc.observer.sinks;

import io.reactivex.rxjava3.core.Observable;
import org.observertc.webrtc.observer.codecs.OutboundReportsCodec;
import org.observertc.webrtc.observer.common.ObservableCollector;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.common.OutboundReportTypeVisitors;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
public class OutboundReportsCollector {

    private static Logger logger = LoggerFactory.getLogger(OutboundReportsCollector.class);

    @Inject
    OutboundReportsCodec outboundReportsCodec;

    @Inject
    ObserverConfig observerConfig;

    private ObservableCollector<OutboundReport> observableCollector;

    @PostConstruct
    void setup() {
        var maxItems = observerConfig.internalCollectors.outboundReports.maxItems;
        var maxTimeInMs = observerConfig.internalCollectors.outboundReports.maxTimeInS * 1000;
        this.observableCollector = ObservableCollector.<OutboundReport>builder()
                .withResilientInput(true)
                .withLogger(logger)
                .withMaxItems(maxItems)
                .withMaxTimeInMs(maxTimeInMs)
                .build();

    }

    @PreDestroy
    void teardown() {
        if (!this.observableCollector.isClosed()) {
            this.observableCollector.onComplete();
        }
    }

    public void add(OutboundReport outboundReport) {
        this.observableCollector.add(outboundReport);
    }

    public void addAll(List<OutboundReport> outboundReports) {
        this.observableCollector.addAll(outboundReports);
    }

    public Observable<List<OutboundReport>> observableOutboundReports() {
        var typeFilter = this.makeOutboundReportPredicate();
        return this.observableCollector.map(items -> {
            return items.stream().filter(typeFilter).collect(Collectors.toList());
        });
    }

    private Predicate<OutboundReport> makeOutboundReportPredicate() {
        var config = this.observerConfig.outboundReports;
        var typeVisitor = OutboundReportTypeVisitors.makeTypeFilter(config);
        return report -> {
            return typeVisitor.apply(null, report.getType());
        };
    }
}