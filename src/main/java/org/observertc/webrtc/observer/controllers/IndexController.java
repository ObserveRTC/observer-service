package org.observertc.webrtc.observer.controllers;///*

import com.hazelcast.map.IMap;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.ObserverHazelcast;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/")
public class IndexController {

	public IndexController() {

	}

	@Get()
	public HttpStatus index() {
		return HttpStatus.OK;
	}

}