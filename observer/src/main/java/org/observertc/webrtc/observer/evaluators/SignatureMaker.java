package org.observertc.webrtc.observer.evaluators;

import io.micronaut.context.annotation.Prototype;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class SignatureMaker {

	private static final Logger logger = LoggerFactory.getLogger(SignatureMaker.class);

	private final MessageDigest digester;

	public SignatureMaker() throws NoSuchAlgorithmException {
		this.digester = MessageDigest.getInstance("SHA-512");
	}

	public <T1> byte[] makeSignature(T1 t1) {
		if (t1 == null) {
			return new byte[0];
		}
		this.digester.reset();
		this.digester.update(t1.toString().getBytes());
		return this.digester.digest();
	}

	public <T1, T2> byte[] makeSignature(T1 t1, T2 t2) {
		if (t1 == null && t2 == null) {
			return new byte[0];
		}
		this.digester.reset();
		if (t1 != null) {
			this.digester.update(t1.toString().getBytes());
		}
		if (t2 != null) {
			this.digester.update(t2.toString().getBytes());
		}
		return this.digester.digest();
	}

	public <T1, T2, T3> byte[] makeSignature(T1 t1, T2 t2, T3 t3) {
		if (t1 == null && t2 == null && t3 == null) {
			return new byte[0];
		}
		this.digester.reset();
		if (t1 != null) {
			this.digester.update(t1.toString().getBytes());
		}
		if (t2 != null) {
			this.digester.update(t2.toString().getBytes());
		}
		if (t3 != null) {
			this.digester.update(t3.toString().getBytes());
		}
		return this.digester.digest();
	}


}