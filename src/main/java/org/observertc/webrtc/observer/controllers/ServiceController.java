package org.observertc.webrtc.observer.controllers;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.observertc.webrtc.observer.entities.ServiceMapEntity;
import org.observertc.webrtc.observer.repositories.ServiceMapsRepository;

import javax.inject.Inject;
import java.util.*;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(value = "/servicemaps")
public class ServiceController {

	@Inject
	ServiceMapsRepository serviceMapsRepository;

	public ServiceController() {

	}

	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Get("/{?name,uuid}")
	public Flowable<ServiceMapEntity> read(Optional<String> name, Optional<UUID> uuid) {
		if (!name.isPresent() && !uuid.isPresent()) {
			var DTOs = this.serviceMapsRepository.findAll().values();
			return Flowable.fromIterable(DTOs);
		}
		Map<String, ServiceMapEntity> result = new HashMap<>();
		if (name.isPresent()) {
			var found = this.serviceMapsRepository.findByName(name.get());
			if (found.isPresent()) {
				result.put(found.get().name, found.get());
			}
		}
		if (uuid.isPresent()) {
			var found = this.serviceMapsRepository.findByUUID(uuid.get());
			if (found.isPresent()) {
				result.put(found.get().name, found.get());
			}
		}
		return Flowable.fromIterable(result.values());
	}


	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Delete("/")
	public HttpResponse delete(String name) {
		try {
			var deleted = this.serviceMapsRepository.delete(name);
			if (Objects.nonNull(deleted)) {
				return HttpResponse.ok(Map.of("deleted", deleted));
			} else {
				return HttpResponse.ok(Map.of("message", String.format("%s not found", name)));
			}
		} catch (Throwable t) {
			return HttpResponse.serverError(Map.of("error", t.getMessage()));
		}
	}

	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Post("/")
	public Single<ServiceMapEntity> create(@Body ServiceMapEntity.DTO serviceMapDTO) {
		return Single.fromCallable(() -> {
			var entity = ServiceMapEntity.from(serviceMapDTO);
			var result = this.serviceMapsRepository.save(entity);
			return result;
		});
	}

	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Put("/")
	public Single<ServiceMapEntity> update(@Body ServiceMapEntity.DTO serviceMapDTO) {
		return Single.fromCallable(() -> {
			var entity = ServiceMapEntity.from(serviceMapDTO);
			var result = this.serviceMapsRepository.update(entity);
			return result;
		});
	}
}