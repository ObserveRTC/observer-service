package org.observertc.webrtc.observer;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import javax.validation.Valid;
import org.observertc.webrtc.observer.dto.ObserverDTO;
import org.observertc.webrtc.observer.repositories.ObserverRepository;

@Secured({SecurityRule.IS_AUTHENTICATED})
@Controller("/observers")
/**
 * The controller responsibility is to call the repositories with 
 * appropriate amount of DTO so it can handle that
 * The controller is responsible to validate the DTOs coming
 */
public class ObserversController {

	private final ObserverRepository observerRepository;

	public ObserversController(ObserverRepository observerRepository) {
		this.observerRepository = observerRepository;
	}

	@Post("/")
	public HttpResponse<UUID> create(@Body @Valid ObserverDTO observerDTO) {
		return this.respond(() -> {
			ObserverDTO result = this.observerRepository.save(observerDTO);
			return HttpResponse.created(result.uuid);
		});
	}

	@Delete("/")
	public HttpResponse delete(@Body UUID observerUUID) {
		return this.respond(() -> {
			this.observerRepository.deleteById(observerUUID);
			return HttpResponse.noContent();
		});

	}

	@Put("/")
	public HttpResponse update(@Body @Valid ObserverDTO observerDTO) {
		return this.respond(() -> {
			ObserverDTO result = this.observerRepository.save(observerDTO);
			return HttpResponse.created(result.uuid);
		});

	}

	@Get("/{observerUUID}")
	public ObserverDTO read(@Body UUID observerUUID) {
		Optional<ObserverDTO> result = this.observerRepository.findById(observerUUID);
		return result.orElse(null);
	}

	private <T> HttpResponse<T> respond(Supplier<HttpResponse<T>> action) {
		try {
			return action.get();
		} catch (Exception ex) {
			return HttpResponse.serverError();
		}
	}

}