package org.observertc.webrtc.observer.controllers;///*

import io.micronaut.http.annotation.Controller;
import org.observertc.webrtc.observer.repositories.ServicesRepository;

import javax.inject.Inject;

//@Secured(SecurityRule.IS_ANONYMOUS)
@Controller(value = "services")
public class ServiceController {

	@Inject
	ServicesRepository servicesRepository;

	public ServiceController() {

	}
//
//	@Post("/")
//	public HttpStatus save(@Body ServiceDTO serviceEntity) {
//		return HttpStatus.OK;
//	}
//
//	@Get("/")
//	public List<ServiceDTO> findAll() {
//		return this.servicesRepository.getAllEntries().values().stream().collect(Collectors.toList());
//	}

}