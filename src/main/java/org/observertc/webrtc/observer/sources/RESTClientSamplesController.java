package org.observertc.webrtc.observer.sources;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.samples.ClientSample;
import org.observertc.webrtc.observer.samples.ObservedClientSample;
import org.observertc.webrtc.observer.samples.ObservedClientSampleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.InvalidObjectException;
import java.util.LinkedList;
import java.util.Objects;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/rest/clientsamples")
public class RESTClientSamplesController {

	private static final Logger logger = LoggerFactory.getLogger(RESTClientSamplesController.class);

	@Inject
	ClientSamplesCollector clientSamplesCollector;

	@Inject
	ExposedMetrics exposedMetrics;

	@PostConstruct
	void setup() {
	}

	@PreDestroy
	void teardown() {
	}

	public RESTClientSamplesController() {

	}

	@Post(value = "/{serviceId}/{mediaUnitId}")
	public HttpResponse<Object> accept(String serviceId, String mediaUnitId, @Body ClientSample[] samples) {
		try {
			if (Objects.isNull(samples) || samples.length < 1) {
				return HttpResponse.ok();
			}
			var observedClientSamples = new LinkedList<ObservedClientSample>();
			for (var sample : samples) {
				var observedClientSample = ObservedClientSampleBuilder.from(sample)
						.withServiceId(serviceId)
						.withMediaUnitId(mediaUnitId)
						.build();
				observedClientSamples.add(observedClientSample);
			}
			try {
				this.clientSamplesCollector.addAll(observedClientSamples);
				this.exposedMetrics.incrementSfuSamplesReceived(serviceId, mediaUnitId, observedClientSamples.size());
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
