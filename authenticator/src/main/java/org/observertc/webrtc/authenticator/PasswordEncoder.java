//package org.observertc.webrtc.authenticator;
//
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.util.Random;
//import javax.inject.Singleton;
//import org.observertc.webrtc.observer.ObserverConfig;
//
//@Singleton
//public class PasswordEncoder {
//
//	private final MessageDigest digest;
//	private final int stretching;
//	private final int saltSize;
//
//	/**
//	 * Constructs an authenticator object keeps connection with the database and validate
//	 * users at login attempt
//	 *
//	 * @throws NoSuchAlgorithmException
//	 */
//	public PasswordEncoder(ObserverConfig.AuthenticationConfig config) throws NoSuchAlgorithmException {
//
//		if (config.stretching < 1) {
//			throw new IllegalArgumentException("The iteration of key stretching cannot be smaller than 1");
//		}
//		this.stretching = config.stretching;
//		this.digest = MessageDigest.getInstance(config.hashAlgorithm);
//		this.saltSize = config.saltSize;
//	}
//
//	/**
//	 * Digest a secret with a salt based on stretching parameter
//	 *
//	 * @param secret
//	 * @param salt
//	 * @return
//	 */
//	public byte[] digest(byte[] secret, byte[] salt) {
//		byte[] result = null;
//		int stretched = 0;
//		do {
//			this.digest.reset();
//			if (result != null) {
//				this.digest.update(result);
//			}
//			this.digest.update(secret);
//			this.digest.update(salt);
//			result = this.digest.digest();
//			++stretched;
//		} while (stretched < this.stretching);
//		return result;
//	}
//
//	public byte[] generateSalt() {
//		Random random = new SecureRandom();
//		byte[] result = new byte[this.saltSize];
//		random.nextBytes(result);
//		return result;
//	}
//}
