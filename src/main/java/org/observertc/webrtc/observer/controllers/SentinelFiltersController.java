package org.observertc.webrtc.observer.controllers;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.observertc.webrtc.observer.dto.CollectionFilterDTO;
import org.observertc.webrtc.observer.dto.SentinelFilterDTO;
import org.observertc.webrtc.observer.repositories.SentinelFiltersRepository;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(value = "/sentinelFilters")
public class SentinelFiltersController {

	@Inject
	SentinelFiltersRepository sentinelFiltersRepository;

	public SentinelFiltersController() {

	}

	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Get("/{?name}")
	public Flowable<Map.Entry<String, SentinelFilterDTO>> read(Optional<String> name) {
		Map<String, SentinelFilterDTO> result = new HashMap<>();
		if (!name.isPresent()) {
			return Flowable.fromIterable(this.sentinelFiltersRepository.findAll().entrySet());
		}
		var found = this.sentinelFiltersRepository.findByName(name.get());
		if (found.isPresent()) {
			result.put(found.get().name, found.get());
		}
		return Flowable.fromIterable(result.entrySet());
	}


	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Delete("/")
	public HttpResponse delete(String name) {
		try {
			var deleted = this.sentinelFiltersRepository.delete(name);
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
	public Single<SentinelFilterDTO> create(@Body SentinelFilterDTO sentinelFilterDTO) {
		if (Objects.isNull(sentinelFilterDTO.peerConnections)) {
			sentinelFilterDTO.peerConnections = new CollectionFilterDTO();
		}
		if (Objects.isNull(sentinelFilterDTO.browserIds)) {
			sentinelFilterDTO.browserIds = new CollectionFilterDTO();
		}
		if (Objects.isNull(sentinelFilterDTO.SSRCs)) {
			sentinelFilterDTO.SSRCs = new CollectionFilterDTO();
		}
		return Single.fromCallable(() -> {
			var result = this.sentinelFiltersRepository.save(sentinelFilterDTO);
			return result;
		});
	}

	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Put("/")
	public Single<SentinelFilterDTO> update(@Body SentinelFilterDTO sentinelFilterDTO) {
		return Single.fromCallable(() -> {
			var result = this.sentinelFiltersRepository.update(sentinelFilterDTO);
			return result;
		});
	}
}