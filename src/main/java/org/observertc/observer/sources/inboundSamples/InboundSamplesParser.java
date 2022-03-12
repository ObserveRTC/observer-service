package org.observertc.observer.sources.inboundSamples;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.observer.samples.ObservedClientSampleBuilder;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.observer.samples.ObservedSfuSampleBuilder;
import org.observertc.schemas.samples.Samples;

import java.util.Objects;

public class InboundSamplesParser implements InboundSamplesAcceptor {

    public static Builder builder() {
        return new Builder();
    }

    private Parser parser = null;
    private Action closer = null;
    private Subject<ObservedClientSample> observedClientSampleSubject = PublishSubject.create();
    private Subject<ObservedSfuSample> observedSfuSampleSubject = PublishSubject.create();


    public Observable<ObservedClientSample> getObservableClientSamples() {
        return this.observedClientSampleSubject;
    }

    public Observable<ObservedSfuSample> getObservableSfuSamples() {
        return this.observedSfuSampleSubject;
    }

    public void accept(String serviceId, String mediaUnitId, byte[] message) throws Throwable {
        Samples samples = this.parser.apply(message);
        if (Objects.nonNull(samples.clientSamples)) {
            Observable.fromArray(samples.clientSamples)
                    .map(sample -> {
                        var result = ObservedClientSampleBuilder.from(sample)
                                .withServiceId(serviceId)
                                .withMediaUnitId(mediaUnitId)
                                .build();
                        return result;
                    })
                    .subscribe(this.observedClientSampleSubject);
        }

        if (Objects.nonNull(samples.sfuSamples)) {
            Observable.fromArray(samples.sfuSamples)
                    .map(sample -> {
                        var result = ObservedSfuSampleBuilder.from(sample)
                                .withServiceId(serviceId)
                                .withMediaUnitId(mediaUnitId)
                                .build();
                        return result;
                    })
                    .subscribe(this.observedSfuSampleSubject);
        }
    }

    @Override
    public void close() throws Throwable {
        if (Objects.nonNull(this.closer)) {
            this.closer.run();
        }
    }


    private InboundSamplesParser() {

    }

    public static class Builder {
        private InboundSamplesParser result = new InboundSamplesParser();
        private Parser parser = null;

        public Builder withParser(Parser parser) {
            this.parser = parser;
            return this;
        }

        public InboundSamplesParser build() {
            Objects.requireNonNull(this.parser);
            Parser decompressedParser = this.parser;
            Action decompressorCloser = () -> {};
            final Parser finalParser = decompressedParser;
            final Action finalCloser = decompressorCloser;
            this.result.parser = finalParser;
            this.result.closer = finalCloser;
            return this.result;
        }
    }
}
