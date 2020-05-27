package com.observertc.gatekeeper;

import com.observertc.gatekeeper.model.UserRole;
import com.observertc.gatekeeper.repositories.UserAuthenticator;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.reactivex.Flowable;
import java.util.Collections;
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
		if (userRole.isEmpty()) {
			return Flowable.just(new AuthenticationFailed(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH));
		}

		var addition = Collections.singletonList(userRole.get().toString());
		return Flowable.just(new UserDetails(username, addition));
	}
}
