package org.observertc.webrtc.observer.service.gatekeeper.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.observertc.webrtc.observer.service.repositories.PasswordEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PasswordEncoderTest {

	/**
	 * Given: A {@link PasswordEncoder}, with saltsize N
	 * <p>
	 * When we generate a salt
	 * <p>
	 * Then the size of the salt is N
	 */
	@Test
	public void shouldMatchSaltSize() throws NoSuchAlgorithmException {
		// Given
		int saltSize = new Random().nextInt(32);
		PasswordEncoder passwordEncoder = new PasswordEncoder("SHA-512", 1, saltSize);

		// When
		byte[] salt = passwordEncoder.generateSalt();

		// Then
		assertEquals(salt.length, saltSize);

	}

	/**
	 * Given two {@link PasswordEncoder}, with different stretching
	 * <p>
	 * When the same password and salt is given to digest
	 * <p>
	 * Then the result is not going to be the same
	 */
	@Test
	public void shouldStretchDifferently() throws NoSuchAlgorithmException {
		// Given
		String password = "password";
		String salt = "salt";
		PasswordEncoder passwordEncoder1 = new PasswordEncoder("SHA-512", 1, 32);
		PasswordEncoder passwordEncoder2 = new PasswordEncoder("SHA-512", 2, 32);

		// When
		byte[] digest1 = passwordEncoder1.digest(password.getBytes(), salt.getBytes());
		byte[] digest2 = passwordEncoder2.digest(password.getBytes(), salt.getBytes());

		// Then
		assertFalse(Arrays.equals(digest1, digest2));
	}

	/**
	 * Given two {@link PasswordEncoder}, with ths same configuration according to the
	 * stretching and to the algorithm
	 * <p>
	 * When we digest the same salt and password,
	 * <p>
	 * Then the result is identical
	 */
	@Test
	public void shouldDeterministic() throws NoSuchAlgorithmException {
		// Given
		String password = "password";
		String salt = "salt";
		PasswordEncoder passwordEncoder1 = new PasswordEncoder("SHA-512", 15, 32);
		PasswordEncoder passwordEncoder2 = new PasswordEncoder("SHA-512", 15, 32);

		// When
		byte[] digest1 = passwordEncoder1.digest(password.getBytes(), salt.getBytes());
		byte[] digest2 = passwordEncoder2.digest(password.getBytes(), salt.getBytes());

		// Then
		assertTrue(Arrays.equals(digest1, digest2));
	}

	/**
	 * Given a not existing hash algorithm
	 * <p>
	 * When we try to construct a {@link PasswordEncoder}
	 * <p>
	 * Then the constructor throws an exception
	 */
	@Test
	public void shouldThrowNoSuchAlgorithmException() {
		String hashAlgorithm = "NoSuchHashAlgorithm";
		assertThrows(NoSuchAlgorithmException.class, () -> new PasswordEncoder(hashAlgorithm, 1, 32));
	}

	/**
	 * Given a stretching number less than 1
	 * <p>
	 * When we try to construct a {@link PasswordEncoder}
	 * <p>
	 * Then the constructor throws an exception
	 */
	@Test()
	public void shouldThrowIllegalArgumentException() {
		final int stretching = 0;
		assertThrows(IllegalArgumentException.class, () -> new PasswordEncoder("SHA-256", stretching, 32));
	}

}