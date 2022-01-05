package org.observertc.webrtc.observer.sources;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.sources.inboundSamples.InboundSamplesAcceptor;
import org.observertc.webrtc.observer.sources.inboundSamples.InboundSamplesAcceptorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Objects;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/rest")
public class SamplesRestApiController {

	private static final Logger logger = LoggerFactory.getLogger(SamplesRestApiController.class);

	@Inject
	ExposedMetrics exposedMetrics;

    private final ObserverConfig.SourcesConfig.RestApiConfig config;
	private final InboundSamplesAcceptor inboundSamplesAcceptor;

	@PostConstruct
	void setup() {
	}

	@PreDestroy
	void teardown() {
	}

	public SamplesRestApiController(ObserverConfig observerConfig, InboundSamplesAcceptorFactory acceptorFactory) {
		this.config = observerConfig.sources.restapi;
		this.inboundSamplesAcceptor = acceptorFactory.makeAcceptor(this.config);
	}

	@Post(value = "/samples/{serviceId}/{mediaUnitId}")
	public HttpResponse<Object> acceptSamples(String serviceId, String mediaUnitId, @Body byte[] message) {
		if (!config.acceptClientSamples) {
			return HttpResponse.serverError("Not accepting client samples through REST");
		}
		if (Objects.isNull(message)) {
			return HttpResponse.ok();
		}
		try {
			this.inboundSamplesAcceptor.accept(serviceId, mediaUnitId, message);
		} catch (IOException e) {
			return HttpResponse.serverError(e.getMessage());
		} catch (Throwable ex) {
			return HttpResponse.serverError(ex.getMessage());
		}
		return HttpResponse.ok();
	}
}
