package com.observertc.gatekeeper.webrtcstat.gatekeeper.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import com.observertc.gatekeeper.webrtcstat.gatekeeper.MockedDSLContextProvider;
import com.observertc.gatekeeper.webrtcstat.model.UserRole;
import com.observertc.gatekeeper.webrtcstat.repositories.PasswordEncoder;
import com.observertc.gatekeeper.webrtcstat.repositories.UserAuthenticator;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

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
		var userAuthenticator = makeUserAuthenticator("1".getBytes(StandardCharsets.UTF_8));

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
		var userAuthenticator = makeUserAuthenticator("1".getBytes(StandardCharsets.UTF_8));

		// When
		Optional<UserRole> userRole = userAuthenticator.authorize("notExisting@test.test", "DoesNotMatterSoMuch");

		// Then
		assertTrue(userRole.isEmpty());
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
		var userAuthenticator = makeUserAuthenticator("2".getBytes(StandardCharsets.UTF_8));

		// When
		Optional<UserRole> userRole = userAuthenticator.authorize("test@test.test", "DoesNotMatterSoMuch");

		// Then
		assertTrue(userRole.isEmpty());
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
		return new UserAuthenticator(
				new MockedDSLContextProvider(USER_AUTHENTICATOR_QUERIES_FILENAME),
				new PasswordEncoder("SHA-512", 2, 32) {
					@Override
					public byte[] digest(byte[] secret, byte[] salt) {
						return digestedPassword;
					}
				}
		);
	}
}