package org.observertc.observer.sources;///*

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.configs.TransportFormatType;
import org.observertc.observer.metrics.SourceMetrics;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;

//@Secured(SecurityRule.IS_AUTHENTICATED)
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/rest")
public class SamplesRestApiController {

	private static final Logger logger = LoggerFactory.getLogger(SamplesRestApiController.class);

	@Inject
	SourceMetrics exposedMetrics;

	@Inject
	SamplesCollector samplesCollector;

    private final ObserverConfig.SourcesConfig.RestConfig config;

	@PostConstruct
	void setup() {

	}

	@PreDestroy
	void teardown() {
	}

	public SamplesRestApiController(ObserverConfig observerConfig) {
		this.config = observerConfig.sources.rest;
	}

	@Post(value = "/samples/{serviceId}/{mediaUnitId}", consumes = MediaType.APPLICATION_OCTET_STREAM)
	public HttpResponse<Object> acceptSamples(
			String serviceId,
			String mediaUnitId,
			@Body byte[] message,
			@Nullable @QueryValue String schemaVersion,
			@Nullable @QueryValue String format
			)
	{
		if (Objects.isNull(message)) {
			return HttpResponse.ok();
		}
		try {
			var version = Utils.firstNotNull(schemaVersion, Samples.VERSION);
			var acceptedFormat = TransportFormatType.getValueOrDefault(format, TransportFormatType.JSON);
			var acceptor = Acceptor.create(
					logger,
					mediaUnitId,
					serviceId,
					version,
					acceptedFormat,
					samplesCollector::accept
			);
//			logger.info("{}\n {}\n", version, Base64.encode(message));
			acceptor.accept(message);
			this.exposedMetrics.incrementRESTReceivedSamples();
		} catch (Throwable ex) {
			return HttpResponse.serverError(ex.getMessage());
		}
		return HttpResponse.ok();
	}
}
