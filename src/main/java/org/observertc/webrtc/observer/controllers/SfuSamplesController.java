package org.observertc.webrtc.observer.controllers;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.rxjava3.core.Observable;
import org.observertc.webrtc.observer.evaluators.ObservedSfuSampleProcessingPipeline;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.samples.ObservedSfuSampleBuilder;
import org.observertc.webrtc.observer.samples.SfuSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.InvalidObjectException;
import java.util.Objects;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/rest/sfusamples")
public class SfuSamplesController {

	private static final Logger logger = LoggerFactory.getLogger(ClientSamplesController.class);

	@Inject
	ObservedSfuSampleProcessingPipeline observedSfuSampleProcessingPipeline;

	@Inject
	ExposedMetrics exposedMetrics;

	@PostConstruct
	void setup() {
	}

	@PreDestroy
	void teardown() {
	}

	public SfuSamplesController() {

	}

	@Post(value = "/{serviceId}/{mediaUnitId}")
	public HttpResponse<Object> accept(String serviceId, String mediaUnitId, @Body SfuSample[] samples) {
		try {
			if (Objects.isNull(samples) || samples.length < 1) {
				return HttpResponse.ok();
			}
			for (var sample : samples) {
				var observedClientSample = ObservedSfuSampleBuilder.from(sample)
						.withServiceId(serviceId)
						.withMediaUnitId(mediaUnitId)
						.build();
				Observable.just(observedClientSample)
						.subscribe(this.observedSfuSampleProcessingPipeline);
			}
			try {
				this.exposedMetrics.incrementSfuSamplesReceived(serviceId, mediaUnitId);
			} catch (Throwable t) {
				logger.warn("MeterRegistry just caused an error by counting samples", t);
			}
			return HttpResponse.ok();
		} catch (InvalidObjectException invalidEx) {
			final String message = invalidEx.getMessage();
			return HttpResponse.serverError(message);
		} catch (Exception ex) {
			return HttpResponse.serverError(ex);
		}
	}

}
