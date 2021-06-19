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
import org.observertc.webrtc.observer.configbuilders.ConfigNode;
import org.observertc.webrtc.observer.configbuilders.ConfigOperations;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.configs.ObserverConfigDispatcher;
import org.observertc.webrtc.observer.repositories.ConfigRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(value = "/config")
public class ConfigController {

	private final ConfigOperations configOperations;
	private final ConfigHolder<ObserverConfig> configHolder;

	@Inject
	ObserverConfigDispatcher observerConfigDispatcher;

	@Inject
	ConfigRepository configRepository;

	public ConfigController(ObserverConfigDispatcher observerConfigDispatcher) throws IOException {
		ObserverConfig config = observerConfigDispatcher.getConfig();
		this.configHolder = new ConfigHolder<ObserverConfig>(config, ObserverConfig.class);
		Map<String, Object> map = ConfigConverter.convertToMap(config);
		this.configOperations = new ConfigOperations(map).withConfigNode(ConfigNode.make(ObserverConfig.class));
	}

	@Secured({"admin"})
	@Get("/{?path}")
	public HttpResponse<Object> read(Optional<String> path) throws IOException {
		try {
			var actual = this.observerConfigDispatcher.getConfig();
			if (path.isEmpty()) {
				return HttpResponse.ok(actual);
			}
			Map<String, Object> actualMap = ConfigConverter.convertToMap(actual);
			String[] pathArr = path.get().split("\\.");
			List<String> pathKeys = List.of(pathArr);
			var result = this.configOperations.replace(actualMap).getPath(pathKeys);
			return HttpResponse.ok(result);
		} catch (Throwable t) {
			var result = t.getMessage();
			return HttpResponse.badRequest(result);
		}
	}

	@Secured({"admin"})
	@Put("/")
	public HttpResponse<Object> update(Map<String, Object> config) throws IOException {
		try {
			ObserverConfig actual = this.observerConfigDispatcher.getConfig();
			Map<String, Object> actualMap = ConfigConverter.convertToMap(actual);
			Map<String, Object> updatedMap = this.configOperations
					.replace(actualMap)
					.add(config)
					.makeConfig();
			List<String> errors = this.configOperations.getErrors();
			if (Objects.nonNull(errors) && 0 < errors.size()) {
				return HttpResponse.badRequest(errors);
			}
			ObserverConfig updatedConfig = ConfigConverter.convert(ObserverConfig.class, updatedMap);
			this.configRepository.updateObserverConfig(updatedConfig);
			var changes = this.updateConfigHolderAndGetChanges(updatedMap);
			return HttpResponse.ok(changes);
		} catch (Throwable t) {
			var error = t.getMessage();
			return HttpResponse.badRequest(error);
		}
	}

	@Secured({"admin"})
	@Delete("/")
	public HttpResponse<Object> remove(String path) throws IOException {
		try {
			String[] keys = path.split("\\.");
			List<String> keyList = Arrays.asList(keys);
			ObserverConfig actual = this.observerConfigDispatcher.getConfig();
			Map<String, Object> actualMap = ConfigConverter.convertToMap(actual);
			Map<String, Object> updatedMap = this.configOperations
					.replace(actualMap)
					.remove(keyList)
					.makeConfig();
			List<String> errors = this.configOperations.getErrors();
			if (Objects.nonNull(errors) && 0 < errors.size()) {
				return HttpResponse.badRequest(errors);
			}
			ObserverConfig updatedConfig = ConfigConverter.convert(ObserverConfig.class, updatedMap);
			this.configRepository.updateObserverConfig(updatedConfig);
			var changes = this.updateConfigHolderAndGetChanges(updatedMap);
			return HttpResponse.ok(changes);
		} catch (Throwable t) {
			var error = t.getMessage();
			return HttpResponse.badRequest(error);
		}
	}

	private Map<String, Map<String, Object>> updateConfigHolderAndGetChanges(Map<String, Object> updatedMap) {
		this.configHolder.renew(updatedMap);
		Map<String, Object> additions = this.configHolder.getAdditions();
		Map<String, Object> removals = this.configHolder.getRemovals();
		var result = Map.of("added", additions, "removed", removals);
		return result;
	}
}