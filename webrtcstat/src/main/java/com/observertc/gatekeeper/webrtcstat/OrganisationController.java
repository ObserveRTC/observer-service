package com.observertc.gatekeeper.webrtcstat;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/organisation")
public class OrganisationController {

	@Get("/")
	public HttpStatus index() {
		return HttpStatus.OK;
	}
}