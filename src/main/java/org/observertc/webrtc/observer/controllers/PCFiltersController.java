package org.observertc.webrtc.observer.controllers;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.observertc.webrtc.observer.dto.CallFilterDTO;
import org.observertc.webrtc.observer.dto.CollectionFilterDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionFilterDTO;
import org.observertc.webrtc.observer.repositories.PeerConnectionFiltersRepository;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(value = "/pcfilters")
public class PCFiltersController {

	@Inject
	PeerConnectionFiltersRepository peerConnectionFiltersRepository;

	public PCFiltersController() {

	}

	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Get("/{?name}")
	public Flowable<Map.Entry<String, PeerConnectionFilterDTO>> read(Optional<String> name) {
		Map<String, PeerConnectionFilterDTO> result = new HashMap<>();
		if (!name.isPresent()) {
			return Flowable.fromIterable(this.peerConnectionFiltersRepository.findAll().entrySet());
		}
		var found = this.peerConnectionFiltersRepository.findByName(name.get());
		if (found.isPresent()) {
			result.put(found.get().name, found.get());
		}
		return Flowable.fromIterable(result.entrySet());
	}


	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Delete("/")
	public HttpResponse delete(String name) {
		try {
			var deleted = this.peerConnectionFiltersRepository.delete(name);
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
	public Single<PeerConnectionFilterDTO> create(@Body PeerConnectionFilterDTO pcFilterDTO) {
		if (Objects.isNull(pcFilterDTO.SSRCs)) {
			pcFilterDTO.SSRCs = new CollectionFilterDTO();
		}
		if (Objects.isNull(pcFilterDTO.remoteIPs)) {
			pcFilterDTO.remoteIPs = new CollectionFilterDTO();
		}
		return Single.fromCallable(() -> {
			var result = this.peerConnectionFiltersRepository.save(pcFilterDTO);
			return result;
		});
	}

	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Put("/")
	public Single<PeerConnectionFilterDTO> update(@Body PeerConnectionFilterDTO pcFilterDTO) {
		return Single.fromCallable(() -> {
			var result = this.peerConnectionFiltersRepository.update(pcFilterDTO);
			return result;
		});
	}
}