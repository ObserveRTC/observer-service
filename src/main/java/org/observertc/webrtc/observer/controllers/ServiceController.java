package org.observertc.webrtc.observer.controllers;///*

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.observertc.webrtc.observer.repositories.ServicesRepository;

import javax.inject.Inject;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(value = "/services")
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
	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Get("/")
	public HttpResponse findAll() {
		return HttpResponse.ok();
	}

}