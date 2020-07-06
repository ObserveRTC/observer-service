package org.observertc.webrtc.observer.service.repositories;

import io.micronaut.context.annotation.Value;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import javax.inject.Singleton;

@Singleton
public class PasswordEncoder {

	private final MessageDigest digest;
	private final int stretching;
	private final int saltSize;

	/**
	 * Constructs an authenticator object keeps connection with the database and validate
	 * users at login attempt
	 *
	 * @param hashAlgorithm the hash algorithm we use to digest
	 * @param stretching    the stretching parameter for the digest algorithm
	 * @throws NoSuchAlgorithmException
	 */
	public PasswordEncoder(
			@Value("${micronaut.application.authentication.hashAlgorithm}") String hashAlgorithm,
			@Value("${micronaut.application.authentication.stretching}") int stretching,
			@Value("${micronaut.application.authentication.saltSize}") int saltSize
	) throws NoSuchAlgorithmException {

		if (stretching < 1) {
			throw new IllegalArgumentException("The iteration of key stretching cannot be smaller than 1");
		}
		this.stretching = stretching;
		this.digest = MessageDigest.getInstance(hashAlgorithm);
		this.saltSize = saltSize;
	}

	/**
	 * Digest a secret with a salt based on stretching parameter
	 *
	 * @param secret
	 * @param salt
	 * @return
	 */
	public byte[] digest(byte[] secret, byte[] salt) {
		byte[] result = null;
		int stretched = 0;
		do {
			this.digest.reset();
			if (result != null) {
				this.digest.update(result);
			}
			this.digest.update(secret);
			this.digest.update(salt);
			result = this.digest.digest();
			++stretched;
		} while (stretched < this.stretching);
		return result;
	}

	public byte[] generateSalt() {
		Random random = new SecureRandom();
		byte[] result = new byte[this.saltSize];
		random.nextBytes(result);
		return result;
	}
}
