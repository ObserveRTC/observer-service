//package org.observertc.webrtc.authenticator;
//
//import java.nio.charset.StandardCharsets;
//import java.security.NoSuchAlgorithmException;
//import java.util.Arrays;
//import java.util.Optional;
//import javax.inject.Singleton;
//import org.jooq.Record3;
//import org.jooq.exception.DataAccessException;
//import org.observertc.webrtc.observer.jooq.Tables;
//import org.observertc.webrtc.observer.jooq.enums.UsersRole;
//import org.observertc.webrtc.observer.model.UserRole;
//
//@Singleton
//public class UserAuthenticator {
//
//	private final PasswordEncoder passwordEncoder;
//	private final IDSLContextProvider dslContextProvider;
//
//	/**
//	 * Constructs an authenticator object keeps connection with the database and validate
//	 * users at login attempt
//	 *
//	 * @param dslContextProvider The Domaiin Specific Language Providr for the database queries
//	 * @throws NoSuchAlgorithmException
//	 */
//	public UserAuthenticator(
//			IDSLContextProvider dslContextProvider,
//			PasswordEncoder passwordEncoder) throws NoSuchAlgorithmException {
//
//		this.dslContextProvider = dslContextProvider;
//		this.passwordEncoder = passwordEncoder;
//	}
//
//	public Optional<UserRole> authorize(String username, String password) {
//		Record3<byte[], byte[], UsersRole> record;
//		try {
//			record = this.dslContextProvider.get()
//					.select(Tables.USERS.PASSWORD_DIGEST, Tables.USERS.PASSWORD_SALT, Tables.USERS.ROLE)
//					.from(Tables.USERS)
//					.where(Tables.USERS.USERNAME.eq(username))
//					.fetchAny();
//		} catch (DataAccessException e) {
//			return Optional.empty();
//		} catch (Exception ex) {
//			return Optional.empty();
//		}
//		if (record == null) {
//			return Optional.empty();
//		}
//		if (!this.authenticate(username, password, record)) {
//			return Optional.empty();
//		}
//
//		UserRole role = UserRole.mapFromDB(record.component3());
//		return Optional.of(role);
//	}
//
//	private boolean authenticate(String username, String password, Record3<byte[], byte[], UsersRole> record) {
//		byte[] storedDigest = record.component1();
//		byte[] storedSalt = record.component2();
//		byte[] providedDigest = this.passwordEncoder.digest(password.getBytes(StandardCharsets.UTF_8), storedSalt);
//		return Arrays.equals(storedDigest, providedDigest);
//	}
//}
