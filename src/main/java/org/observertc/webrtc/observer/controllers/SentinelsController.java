package org.observertc.webrtc.observer.controllers;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.observertc.webrtc.observer.dto.SentinelDTO;
import org.observertc.webrtc.observer.repositories.SentinelsRepository;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(value = "/sentinels")
public class SentinelsController {

	@Inject
	SentinelsRepository sentinelsRepository;

	public SentinelsController() {

	}

	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Get("/{?name}")
	public Flowable<Map.Entry<String, SentinelDTO>> read(Optional<String> name) {
		Map<String, SentinelDTO> result = new HashMap<>();
		if (!name.isPresent()) {
			return Flowable.fromIterable(this.sentinelsRepository.findAll().entrySet());
		}
		var found = this.sentinelsRepository.findByName(name.get());
		if (found.isPresent()) {
			result.put(found.get().name, found.get());
		}
		return Flowable.fromIterable(result.entrySet());
	}


	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Delete("/")
	public HttpResponse delete(String name) {
		try {
			var deleted = this.sentinelsRepository.delete(name);
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
	public Single<SentinelDTO> create(@Body SentinelDTO sentinelDTO) {
		return Single.fromCallable(() -> {
			var result = this.sentinelsRepository.save(sentinelDTO);
			return result;
		});
	}

	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Put("/")
	public Single<SentinelDTO> update(@Body SentinelDTO sentinelDTO) {
		return Single.fromCallable(() -> {
			var result = this.sentinelsRepository.update(sentinelDTO);
			return result;
		});
	}
}