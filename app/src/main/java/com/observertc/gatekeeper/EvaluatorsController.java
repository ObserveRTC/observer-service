package com.observertc.gatekeeper;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/evaluators")
public class EvaluatorsController {

	@Get("/")
	public HttpStatus index() {
		return HttpStatus.OK;
	}

}