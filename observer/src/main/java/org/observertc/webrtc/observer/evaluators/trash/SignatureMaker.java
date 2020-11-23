///*
// * Copyright  2020 Balazs Kreith
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.observertc.webrtc.observer.evaluators;
//
//import io.micronaut.context.annotation.Prototype;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@Prototype
//public class SignatureMaker {
//
//	private static final Logger logger = LoggerFactory.getLogger(SignatureMaker.class);
//
//	private final MessageDigest digester;
//
//	public SignatureMaker() throws NoSuchAlgorithmException {
//		this.digester = MessageDigest.getInstance("SHA-512");
//	}
//
//	public <T1> byte[] makeSignature(T1 t1) {
//		if (t1 == null) {
//			return new byte[0];
//		}
//		this.digester.reset();
//		this.digester.update(t1.toString().getBytes());
//		return this.digester.digest();
//	}
//
//	public <T1, T2> byte[] makeSignature(T1 t1, T2 t2) {
//		if (t1 == null && t2 == null) {
//			return new byte[0];
//		}
//		this.digester.reset();
//		if (t1 != null) {
//			this.digester.update(t1.toString().getBytes());
//		}
//		if (t2 != null) {
//			this.digester.update(t2.toString().getBytes());
//		}
//		return this.digester.digest();
//	}
//
//	public <T1, T2, T3> byte[] makeSignature(T1 t1, T2 t2, T3 t3) {
//		if (t1 == null && t2 == null && t3 == null) {
//			return new byte[0];
//		}
//		this.digester.reset();
//		if (t1 != null) {
//			this.digester.update(t1.toString().getBytes());
//		}
//		if (t2 != null) {
//			this.digester.update(t2.toString().getBytes());
//		}
//		if (t3 != null) {
//			this.digester.update(t3.toString().getBytes());
//		}
//		return this.digester.digest();
//	}
//
//
//}