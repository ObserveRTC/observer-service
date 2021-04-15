package org.observertc.webrtc.observer.controllers;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.observertc.webrtc.observer.configbuilders.ConfigConverter;
import org.observertc.webrtc.observer.configbuilders.ConfigHolder;
import org.observertc.webrtc.observer.configbuilders.ConfigOperations;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.configs.ObserverConfigDispatcher;
import org.observertc.webrtc.observer.repositories.ConfigRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(value = "/config")
public class ConfigController {

	private final ConfigOperations configOperations;

	@Inject
	ObserverConfigDispatcher observerConfigDispatcher;

	@Inject
	ConfigRepository configRepository;

	public ConfigController(ObserverConfigDispatcher observerConfigDispatcher) throws IOException {
		ObserverConfig config = observerConfigDispatcher.getConfig();
		Map<String, Object> map = ConfigConverter.convertToMap(config);
		this.configOperations = new ConfigOperations(map);
	}

	@Get("/")
	public HttpResponse<Object> read() throws IOException {
		try {
			var result = this.observerConfigDispatcher.getConfig();
			return HttpResponse.ok(result);
		} catch (Throwable t) {
			var result = t.getMessage();
			return HttpResponse.badRequest(result);
		}
	}

	@Put("/")
	public HttpResponse<Object> update(Map<String, Object> config) throws IOException {
		try {
			ObserverConfig actual = this.observerConfigDispatcher.getConfig();
			Map<String, Object> actualMap = ConfigConverter.convertToMap(actual);
			Map<String, Object> updatedMap = this.configOperations
					.replace(actualMap)
					.add(config)
					.makeConfig();
			ObserverConfig updatedConfig = ConfigConverter.convert(ObserverConfig.class, updatedMap);
			this.configRepository.updateObserverConfig(updatedConfig);
			return HttpResponse.ok(updatedConfig);
		} catch (Throwable t) {
			var result = t.getMessage();
			return HttpResponse.badRequest(result);
		}
	}

	@Delete("/")
	public HttpResponse<Object> remove(String path) throws IOException {
		try {
			ObserverConfig actual = this.observerConfigDispatcher.getConfig();
			Map<String, Object> actualMap = ConfigConverter.convertToMap(actual);
			Map<String, Object> updatedMap = this.configOperations
					.replace(actualMap)
					.remove(Arrays.asList(path.split(ConfigHolder.DEFAULT_DELIMITER)))
					.makeConfig();
			ObserverConfig updatedConfig = ConfigConverter.convert(ObserverConfig.class, updatedMap);
			this.configRepository.updateObserverConfig(updatedConfig);
			return HttpResponse.ok(updatedConfig);
		} catch (Throwable t) {
			var result = t.getMessage();
			return HttpResponse.badRequest(result);
		}
	}
}