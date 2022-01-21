package org.observertc.webrtc.observer.sources.inboundSamples;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.observertc.webrtc.observer.compressors.DecompressorBuilder;
import org.observertc.webrtc.observer.micrometer.MonitorProvider;
import org.observertc.webrtc.observer.samples.Samples;
import org.observertc.webrtc.observer.sources.ClientSamplesCollector;
import org.observertc.webrtc.observer.sources.SfuSamplesCollector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public class InboundSamplesAcceptorFactory {

    private final ObjectMapper objectMapper;

    @Inject
    ClientSamplesCollector clientSamplesCollector;

    @Inject
    SfuSamplesCollector sfuSamplesCollector;

    public InboundSamplesAcceptorFactory(
            ObjectMapper objectMapper,
            MonitorProvider monitorProvider
    ) {
        this.objectMapper = objectMapper;
//        this.flawMonitor = monitorProvider.makeFlawMonitorFor(this.getClass()).withDefaultLogger(logger).withDefaultLogLevel(Level.WARN);
    }

    public InboundSamplesAcceptor makeAcceptor(InboundSamplesConfig config) {
        var parserBuilder = InboundSamplesParser.builder();

        // add decompressor if it is configured
        if (Objects.nonNull(config.decompressor)) {
            var decompressorBuilder = new DecompressorBuilder();
            decompressorBuilder.withConfiguration(config.decompressor);
            var decompressor = decompressorBuilder.build();
            parserBuilder.withDecompressor(decompressor);
        }

        // add the sample parser base function
        var reader = this.objectMapper.reader();
        Parser parser = message -> reader.readValue(message, Samples.class);
        parserBuilder.withParser(parser);
        var samplesParser = parserBuilder.build();

        // pipe the necessary stream components
        samplesParser.getObservableClientSamples().subscribe(this.clientSamplesCollector::add);
        samplesParser.getObservableSfuSamples().subscribe(this.sfuSamplesCollector::add);

        // make acceptor
        return samplesParser;
    }
}
