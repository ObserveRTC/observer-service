package org.observertc.webrtc.observer.controllers;///*

import io.micrometer.core.annotation.Timed;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.observertc.webrtc.observer.configs.ObserverConfig;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(value = "/config")
public class ConfigController {

	@Inject
	ObserverConfig baseConfig;

	@Secured({"admin"})
	@Get("/base")
	public HttpResponse<ObserverConfig> getBaseConfig() throws IOException {
		return HttpResponse.ok(baseConfig);
	}

	@Secured({"admin"})
	@Get("/obfuscations")
	public HttpResponse<Object> getObfuscations() throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Secured({"admin"})
	@Put("/obfuscations")
	public HttpResponse<Object> setObfuscations(Map<String, Object> config) throws IOException {
		throw new RuntimeException("Not implemented");
	}

}