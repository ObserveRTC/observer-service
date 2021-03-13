package org.observertc.webrtc.observer.controllers;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.observertc.webrtc.observer.dto.CollectionFilterDTO;
import org.observertc.webrtc.observer.dto.CallFilterDTO;
import org.observertc.webrtc.observer.repositories.CallFiltersRepository;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(value = "/callfilters")
public class CallFiltersController {

	@Inject
	CallFiltersRepository callFiltersRepository;

	public CallFiltersController() {

	}

	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Get("/{?name}")
	public Flowable<Map.Entry<String, CallFilterDTO>> read(Optional<String> name) {
		Map<String, CallFilterDTO> result = new HashMap<>();
		if (!name.isPresent()) {
			return Flowable.fromIterable(this.callFiltersRepository.findAll().entrySet());
		}
		var found = this.callFiltersRepository.findByName(name.get());
		if (found.isPresent()) {
			result.put(found.get().name, found.get());
		}
		return Flowable.fromIterable(result.entrySet());
	}


	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Delete("/")
	public HttpResponse delete(String name) {
		try {
			var deleted = this.callFiltersRepository.delete(name);
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
	public Single<CallFilterDTO> create(@Body CallFilterDTO callFilterDTO) {
		if (Objects.isNull(callFilterDTO.peerConnections)) {
			callFilterDTO.peerConnections = new CollectionFilterDTO();
		}
		if (Objects.isNull(callFilterDTO.browserIds)) {
			callFilterDTO.browserIds = new CollectionFilterDTO();
		}
		return Single.fromCallable(() -> {
			var result = this.callFiltersRepository.save(callFilterDTO);
			return result;
		});
	}

	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Put("/")
	public Single<CallFilterDTO> update(@Body CallFilterDTO callFilterDTO) {
		return Single.fromCallable(() -> {
			var result = this.callFiltersRepository.update(callFilterDTO);
			return result;
		});
	}
}