package org.observertc.webrtc.observer.service;

import org.observertc.webrtc.observer.service.model.UserRole;
import org.observertc.webrtc.observer.service.repositories.UserAuthenticator;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.reactivex.Flowable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Singleton;
import org.reactivestreams.Publisher;

@Singleton
public class AuthenticationProviderImpl implements AuthenticationProvider {

	private final UserAuthenticator userAuthenticator;

	public AuthenticationProviderImpl(UserAuthenticator userAuthenticator) {
		this.userAuthenticator = userAuthenticator;
	}


	@Override
	public Publisher<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {

		final String username = authenticationRequest.getIdentity().toString();
		final String password = authenticationRequest.getSecret().toString();
		Optional<UserRole> userRole = this.userAuthenticator.authorize(username, password);
		if (!userRole.isPresent()) {
			return Flowable.just(new AuthenticationFailed(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH));
		}

		List<String> addition = Collections.singletonList(userRole.get().toString());
		return Flowable.just(new UserDetails(username, addition));
	}
}
