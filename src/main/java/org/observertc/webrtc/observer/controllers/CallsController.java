package org.observertc.webrtc.observer.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.inject.Inject;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/calls")
public class CallsController {

    @Inject
    HazelcastMaps hazelcastMaps;

    public CallsController() {

    }

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Get("/remoteIPs")
    public HttpResponse getAllRemoteIPs() {
        try {
            var remoteIps = this.hazelcastMaps.getRemoteIPToPCs().keySet();
            return HttpResponse.ok(remoteIps);
        } catch (Throwable t) {
            return HttpResponse.serverError(t.getMessage());
        }
    }
}
