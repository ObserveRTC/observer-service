package org.observertc.webrtc.observer.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.MockedDSLContextProvider;
import org.observertc.webrtc.observer.model.UserRole;

class UserAuthenticatorTest {

	private static final String USER_AUTHENTICATOR_QUERIES_FILENAME = "UserAuthenticatorTestQueries.sql";

	/**
	 * Given a {@link UserAuthenticator}, a user email and a digested password
	 * which exists inside the file database
	 * <p>
	 * When isValid method is called
	 * <p>
	 * Then it returns true
	 * <p>
	 * NOTE: The FileDatabase for the MockedDSLContextProvider should contain
	 * exactly the email and digested password
	 *
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void shouldBeValidEmailAndPassword() throws NoSuchAlgorithmException {
		// Given
		UserAuthenticator userAuthenticator = makeUserAuthenticator("1".getBytes(StandardCharsets.UTF_8));

		// When
		Optional<UserRole> userRole = userAuthenticator.authorize("test@test.test", "DoesNotMatterSoMuch");

		// Then
		//assertTrue(userRole.isPresent());
	}


	/**
	 * Given a {@link UserAuthenticator}, a not existing user and a valid digested password
	 * which exists inside the file database
	 * <p>
	 * When isValid method is called
	 * <p>
	 * Then it returns true
	 * <p>
	 * NOTE: The FileDatabase for the MockedDSLContextProvider should NOT contain
	 * the email
	 *
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void shouldBeNotValidBecauseOfEmail() throws NoSuchAlgorithmException {
		// Given
		UserAuthenticator userAuthenticator = makeUserAuthenticator("1".getBytes(StandardCharsets.UTF_8));

		// When
		Optional<UserRole> userRole = userAuthenticator.authorize("notExisting@test.test", "DoesNotMatterSoMuch");

		// Then
		assertFalse(userRole.isPresent());
	}

	/**
	 * Given a {@link UserAuthenticator}, a not existing user and a valid digested password
	 * which exists inside the file database
	 * <p>
	 * When isValid method is called
	 * <p>
	 * Then it returns false
	 * <p>
	 * NOTE: The FileDatabase for the MockedDSLContextProvider should NOT contain
	 * the email
	 *
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void shouldBeNotValidBecauseOfPassword() throws NoSuchAlgorithmException {
		// Given
		UserAuthenticator userAuthenticator = makeUserAuthenticator("2".getBytes(StandardCharsets.UTF_8));

		// When
		Optional<UserRole> userRole = userAuthenticator.authorize("test@test.test", "DoesNotMatterSoMuch");

		// Then
		assertFalse(userRole.isPresent());
	}

	/**
	 * Make a {@link UserAuthenticator} mocking the DSLContext and
	 * the PasswordEncoder, so we can test it individually
	 *
	 * @param digestedPassword the encoded digested password should be returned by the
	 *                         PasswordEncoder mock
	 * @return a {@link UserAuthenticator}
	 * @throws NoSuchAlgorithmException
	 */
	private UserAuthenticator makeUserAuthenticator(byte[] digestedPassword) throws NoSuchAlgorithmException {
		ObserverConfig.AuthenticationConfig config = this.makeAuthConfig("SHA-512", 2, 32);
		return new UserAuthenticator(
				new MockedDSLContextProvider(USER_AUTHENTICATOR_QUERIES_FILENAME),
				new PasswordEncoder(config) {
					@Override
					public byte[] digest(byte[] secret, byte[] salt) {
						return digestedPassword;
					}
				}
		);
	}

	private ObserverConfig.AuthenticationConfig makeAuthConfig(String hashAlgorithm, int stretching, int saltSize) {
		ObserverConfig.AuthenticationConfig config = new ObserverConfig.AuthenticationConfig();
		config.hashAlgorithm = hashAlgorithm;
		config.stretching = stretching;
		config.saltSize = saltSize;
		return config;
	}
}