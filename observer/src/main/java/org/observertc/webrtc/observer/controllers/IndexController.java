package org.observertc.webrtc.observer.controllers;///*

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import javax.inject.Inject;

@Controller("/")
public class IndexController {

	@Inject
	public IndexController() {
	}

//	@Get
//	@Produces("text/plain")
//	public String metrics() {
//		return prometheusMeterRegistry.scrape();
//	}

	@Get()
	public HttpStatus index() {
		return HttpStatus.OK;
	}
}