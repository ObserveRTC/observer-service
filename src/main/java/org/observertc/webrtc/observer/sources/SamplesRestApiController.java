package org.observertc.webrtc.observer.sources;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.samples.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.InvalidObjectException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/rest")
public class SamplesRestApiController {

	private static final Logger logger = LoggerFactory.getLogger(SamplesRestApiController.class);

	@Inject
	ClientSamplesCollector clientSamplesCollector;

	@Inject
	SfuSamplesCollector sfuSamplesCollector;

	@Inject
	ExposedMetrics exposedMetrics;

	@Inject
    ObserverConfig.SourcesConfig.RestApiConfig config;

	@PostConstruct
	void setup() {
	}

	@PreDestroy
	void teardown() {
	}

	public SamplesRestApiController() {

	}

	@Post(value = "/samples/{serviceId}/{mediaUnitId}")
	public HttpResponse<Object> acceptSamples(String serviceId, String mediaUnitId, @Body Samples samples) {
		if (!config.acceptClientSamples) {
			return HttpResponse.serverError("Not accepting client samples through REST");
		}
		try {
			if (Objects.isNull(samples)) {
				return HttpResponse.ok();
			}
			List<HttpResponse<Object>> responses = new LinkedList<>();
			if (Objects.nonNull(samples.clientSamples)) {
				responses.add(this.acceptClientSamples(serviceId, mediaUnitId, samples.clientSamples));
			}
			if (Objects.nonNull(samples.sfuSamples)) {
				responses.add(this.acceptSfuSamples(serviceId, mediaUnitId, samples.sfuSamples));
			}
			if (responses.size() < 1) {
				return HttpResponse.ok();
			}
			var nokResponseHolder = responses.stream().filter(response -> response.code() != HttpResponse.ok().code()).findFirst();
			if (nokResponseHolder.isPresent()) {
				return nokResponseHolder.get();
			}
			return HttpResponse.ok();
		} catch (Exception ex) {
			return HttpResponse.serverError(ex);
		}
	}

	@Post(value = "/clientsamples/{serviceId}/{mediaUnitId}")
	public HttpResponse<Object> acceptClientSamples(String serviceId, String mediaUnitId, @Body ClientSample[] samples) {
        if (!config.acceptClientSamples) {
            return HttpResponse.serverError("Not accepting client samples through REST");
        }
		try {
			if (Objects.isNull(samples) || samples.length < 1) {
				return HttpResponse.ok();
			}
            if (config.maxClientSamplesBatch < samples.length) {
                return HttpResponse.badRequest("size of batch is too large. Maximum size is: " + config.maxClientSamplesBatch);
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

	@Post(value = "/sfusamples/{serviceId}/{mediaUnitId}")
	public HttpResponse<Object> acceptSfuSamples(String serviceId, String mediaUnitId, @Body SfuSample[] samples) {
        if (!config.acceptSfuSamples) {
            return HttpResponse.serverError("Not accepting sfu samples through REST");
        }
		try {
			if (Objects.isNull(samples) || samples.length < 1) {
				return HttpResponse.ok();
			}
            if (config.maxSfuSamplesBatch < samples.length) {
                return HttpResponse.badRequest("size of batch is too large. Maximum size is: " + config.maxClientSamplesBatch);
            }
			var observedSfuSamples = new LinkedList<ObservedSfuSample>();
			for (var sample : samples) {
				var observedSfuSample = ObservedSfuSampleBuilder.from(sample)
						.withServiceId(serviceId)
						.withMediaUnitId(mediaUnitId)
						.build();
				observedSfuSamples.add(observedSfuSample);
			}

			try {
				this.sfuSamplesCollector.addAll(observedSfuSamples);
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
