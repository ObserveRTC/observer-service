package org.observertc.observer.sources;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.mappings.Decoder;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Objects;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/rest")
public class SamplesRestApiController {

	private static final Logger logger = LoggerFactory.getLogger(SamplesRestApiController.class);

	@Inject
	ExposedMetrics exposedMetrics;

	@Inject
	SamplesCollector samplesCollector;

    private final ObserverConfig.SourcesConfig.RestConfig config;
	private final Decoder<byte[], Samples> decoder;

	@PostConstruct
	void setup() {

	}

	@PreDestroy
	void teardown() {
	}

	public SamplesRestApiController(ObserverConfig observerConfig) {
		this.config = observerConfig.sources.rest;
		this.decoder = new SamplesDecoderBuilder()
				.withCodecType(this.config.decoder)
				.build();
	}

	@Post(value = "/samples/{serviceId}/{mediaUnitId}")
	public HttpResponse<Object> acceptSamples(String serviceId, String mediaUnitId, @Body byte[] message) {
		if (Objects.isNull(message)) {
			return HttpResponse.ok();
		}
		try {
			var samples = this.decoder.decode(message);
			var receivedSample = ReceivedSamples.of(
					serviceId,
					mediaUnitId,
					samples
			);
			this.samplesCollector.accept(receivedSample);
		} catch (Throwable ex) {
			return HttpResponse.serverError(ex.getMessage());
		}
		return HttpResponse.ok();
	}
}
