//package org.observertc.webrtc.observer.repositories;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import java.security.NoSuchAlgorithmException;
//import java.util.Arrays;
//import java.util.Random;
//import org.junit.jupiter.api.Test;
//import org.observertc.webrtc.observer.ObserverConfig;
//
//class PasswordEncoderTest {
//
//	/**
//	 * Given: A {@link PasswordEncoder}, with saltsize N
//	 * <p>
//	 * When we generate a salt
//	 * <p>
//	 * Then the size of the salt is N
//	 */
//	@Test
//	public void shouldMatchSaltSize() throws NoSuchAlgorithmException {
//		// Given
//		int saltSize = new Random().nextInt(32);
//		ObserverConfig.AuthenticationConfig config = this.makeConfig("SHA-512", 1, saltSize);
//		PasswordEncoder passwordEncoder = new PasswordEncoder(config);
//
//		// When
//		byte[] salt = passwordEncoder.generateSalt();
//
//		// Then
//		assertEquals(salt.length, saltSize);
//
//	}
//
//	/**
//	 * Given two {@link PasswordEncoder}, with different stretching
//	 * <p>
//	 * When the same password and salt is given to digest
//	 * <p>
//	 * Then the result is not going to be the same
//	 */
//	@Test
//	public void shouldStretchDifferently() throws NoSuchAlgorithmException {
//		// Given
//		String password = "password";
//		String salt = "salt";
//		ObserverConfig.AuthenticationConfig config1 = this.makeConfig("SHA-512", 1, 32);
//		ObserverConfig.AuthenticationConfig config2 = this.makeConfig("SHA-512", 2, 32);
//		PasswordEncoder passwordEncoder1 = new PasswordEncoder(config1);
//		PasswordEncoder passwordEncoder2 = new PasswordEncoder(config2);
//
//		// When
//		byte[] digest1 = passwordEncoder1.digest(password.getBytes(), salt.getBytes());
//		byte[] digest2 = passwordEncoder2.digest(password.getBytes(), salt.getBytes());
//
//		// Then
//		assertFalse(Arrays.equals(digest1, digest2));
//	}
//
//	/**
//	 * Given two {@link PasswordEncoder}, with ths same configuration according to the
//	 * stretching and to the algorithm
//	 * <p>
//	 * When we digest the same salt and password,
//	 * <p>
//	 * Then the result is identical
//	 */
//	@Test
//	public void shouldDeterministic() throws NoSuchAlgorithmException {
//		// Given
//		String password = "password";
//		String salt = "salt";
//		ObserverConfig.AuthenticationConfig config = this.makeConfig("SHA-512", 15, 32);
//		PasswordEncoder passwordEncoder1 = new PasswordEncoder(config);
//		PasswordEncoder passwordEncoder2 = new PasswordEncoder(config);
//
//		// When
//		byte[] digest1 = passwordEncoder1.digest(password.getBytes(), salt.getBytes());
//		byte[] digest2 = passwordEncoder2.digest(password.getBytes(), salt.getBytes());
//
//		// Then
//		assertTrue(Arrays.equals(digest1, digest2));
//	}
//
//	/**
//	 * Given a not existing hash algorithm
//	 * <p>
//	 * When we try to construct a {@link PasswordEncoder}
//	 * <p>
//	 * Then the constructor throws an exception
//	 */
//	@Test
//	public void shouldThrowNoSuchAlgorithmException() {
//		String hashAlgorithm = "NoSuchHashAlgorithm";
//		ObserverConfig.AuthenticationConfig config = this.makeConfig(hashAlgorithm, 1, 32);
//		assertThrows(NoSuchAlgorithmException.class, () -> new PasswordEncoder(config));
//	}
//
//	/**
//	 * Given a stretching number less than 1
//	 * <p>
//	 * When we try to construct a {@link PasswordEncoder}
//	 * <p>
//	 * Then the constructor throws an exception
//	 */
//	@Test()
//	public void shouldThrowIllegalArgumentException() {
//		final int stretching = 0;
//		ObserverConfig.AuthenticationConfig config = this.makeConfig("SHA-256", stretching, 32);
//		assertThrows(IllegalArgumentException.class, () -> new PasswordEncoder(config));
//	}
//
//	private ObserverConfig.AuthenticationConfig makeConfig(String hashAlgorithm, int stretching, int saltSize) {
//		ObserverConfig.AuthenticationConfig config = new ObserverConfig.AuthenticationConfig();
//		config.hashAlgorithm = hashAlgorithm;
//		config.stretching = stretching;
//		config.saltSize = saltSize;
//		return config;
//	}
//}