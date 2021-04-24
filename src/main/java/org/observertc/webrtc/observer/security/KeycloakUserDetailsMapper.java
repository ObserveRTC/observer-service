package org.observertc.webrtc.observer.security;

import io.micrometer.core.lang.Nullable;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.OauthUserDetailsMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Named("keycloak")
@Singleton
public class KeycloakUserDetailsMapper implements OauthUserDetailsMapper {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakUserDetailsMapper.class);

    @Property(name = "micronaut.security.oauth2.clients.keycloak.client-id")
    private String clientId;
    @Property(name = "micronaut.security.oauth2.clients.keycloak.client-secret")
    private String clientSecret;

    @Client("http://localhost:8888")
    @Inject
    private RxHttpClient client;

    @Override
    public Publisher<UserDetails> createUserDetails(TokenResponse tokenResponse) {
        return Publishers.just(new UnsupportedOperationException());
    }

    @Override
    public Publisher<AuthenticationResponse> createAuthenticationResponse(
            TokenResponse tokenResponse, @Nullable State state) {
        Flowable<HttpResponse<KeycloakUser>> res = client
                .exchange(HttpRequest.POST("/auth/realms/master/protocol/openid-connect/token/introspect",
                        "token=" + tokenResponse.getAccessToken())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .basicAuth(clientId, clientSecret), KeycloakUser.class);
        return res.map(user -> {
            logger.info("User: {}", user.body());
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("openIdToken", tokenResponse.getAccessToken());
            return new UserDetails(user.body().username, user.body().roles, attrs);
        });
    }

}
