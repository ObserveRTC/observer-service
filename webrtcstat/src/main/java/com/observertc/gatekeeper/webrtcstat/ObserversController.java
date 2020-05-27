package com.observertc.gatekeeper.webrtcstat;

import com.observertc.gatekeeper.dto.ObserverDTO;
import com.observertc.gatekeeper.webrtcstat.repositories.ObserverRepository;
import com.observertc.gatekeeper.dto.EvaluatorDTO;
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
	public ObserverDTO readObserver(@Body UUID observerUUID) {
		Optional<ObserverDTO> result = this.observerRepository.findById(observerUUID);
		return result.orElse(null);
	}

	@Post("/{observerUUID}/evaluators/")
	public HttpResponse<UUID> addEvaluator(
			@Body UUID observerUUID,
			@Body UUID evaluatorUUID) {
		return null;
	}

	@Get("/{observerUUID}/evaluators")
	public Iterable<EvaluatorDTO> readObserverEvaluators(@Body UUID observerUUID) {
		Iterable<EvaluatorDTO> result = this.observerRepository.findEvaluators(observerUUID);
		return result;
	}


	private <T> HttpResponse<T> respond(Supplier<HttpResponse<T>> action) {
		try {
			return action.get();
		} catch (Exception ex) {
			return HttpResponse.serverError();
		}
	}

}