package org.observertc.observer.controllers;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import org.observertc.observer.configs.ObserverConfig;

import java.io.IOException;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(value = "/config")
public class ConfigController {

	@Inject
    ObserverConfig baseConfig;

	@Secured({"admin"})
	@Get("/")
	public HttpResponse<ObserverConfig> getBaseConfig() throws IOException {
		if (this.baseConfig.security.allowExposeConfig) {
			return HttpResponse.ok(baseConfig);
		} else {
			return HttpResponse.ok();
		}
	}
}