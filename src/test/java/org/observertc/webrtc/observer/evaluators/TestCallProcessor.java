package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Replaces;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.samples.ObservedClientSampleGenerator;
import org.observertc.webrtc.schemas.reports.ReportType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.*;

@Prototype
class TestCallProcessor {

    private Subject<OutboundReport> reportCollector = PublishSubject.create();
    private Map<ReportType, List<OutboundReport>> receivedReports = new HashMap<>();
    private CompletableFuture<Void> endedTest;

    private String serviceId = "myService";
    private String mediaUnitId = "myApp";
    private String roomId = "myTest";
    private List<ClientSurrogate> clientSurrogates = new LinkedList<>();

    @Inject
    ProcessingPipeline pipeline;

    @Inject
    ObservedClientSampleGenerator alice;

    @Inject
    ObservedClientSampleGenerator bob;

    private MockedOutboundReportEncoder mockedOutboundReportEncoder;
    private ExecutorService executors;

    public TestCallProcessor(MockedOutboundReportEncoder mockedOutboundReportEncoder) {
        this.mockedOutboundReportEncoder = mockedOutboundReportEncoder;
        this.receivedReports.clear();
        this.endedTest = new CompletableFuture<>();
        this.mockedOutboundReportEncoder.getTestObservableOutboundReport().subscribe(this.reportCollector);
        this.reportCollector.map(report -> {
            List<OutboundReport> reports = this.receivedReports.get(report.getType());
            if (Objects.isNull(reports)) {
                reports = new LinkedList<>();
                this.receivedReports.put(report.getType(), reports);
            }
            reports.add(report);
            return true;
        }).debounce(10, TimeUnit.SECONDS)
                .subscribe(something -> {
                    endedTest.complete(null);
                });

    }

    TestCallProcessor withServiceId(String value) {
        this.serviceId = value;
        return this;
    }

    TestCallProcessor withMediaUnitId(String value) {
        this.mediaUnitId = value;
        return this;
    }

    TestCallProcessor withRoomId(String value) {
        this.roomId = value;
        return this;
    }

    TestCallProcessor withClientSurrogate(ClientSurrogate clientSurrogate) {
        this.clientSurrogates.add(clientSurrogate);
        return this;
    }

    TestCallProcessor start() {
        if (this.clientSurrogates.size() < 1) {
            throw new RuntimeException("At least one participant is necessary to perform a test");
        }
        Random random = new Random();
        this.executors = Executors.newFixedThreadPool(this.clientSurrogates.size());
        this.clientSurrogates.stream().forEach(generator -> {
            generator
                    .withServiceId(this.serviceId)
                    .withMediaUnitId(this.mediaUnitId)
                    .withRoomId(this.roomId);

            executors.submit(() -> {
                int delayInMs = random.nextInt(300) + 200;
                Observable.range(0, random.nextInt(10) + 5)
                    .delay(delayInMs, TimeUnit.MILLISECONDS)
                    .map(i -> generator.get())
                    .subscribe(this.pipeline)
                ;
            });
        });
        return this;
    }

    TestCallProcessor waitUntilEnd(long timeoutInMin) throws ExecutionException, InterruptedException, TimeoutException {
        this.endedTest.get(timeoutInMin, TimeUnit.MINUTES);
        return this;
    }

    @Replaces(OutboundReportEncoder.class)
    @Singleton
    public static class MockedOutboundReportEncoder extends OutboundReportEncoder{

        public MockedOutboundReportEncoder(ObserverConfig config) {
            super(config);
        }

        @Override
        public Observable<OutboundReport> getObservableOutboundReport() {
            return PublishSubject.create();
        }

        public Observable<OutboundReport> getTestObservableOutboundReport() {
            return super.getObservableOutboundReport();
        }
    }
}