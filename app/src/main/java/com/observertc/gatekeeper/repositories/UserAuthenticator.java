package com.observertc.gatekeeper.repositories;

import static com.observertc.gatekeeper.jooq.Tables.USERS;
import com.observertc.gatekeeper.jooq.enums.UsersRole;
import com.observertc.gatekeeper.model.UserRole;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import javax.inject.Singleton;
import org.jooq.Record3;
import org.jooq.exception.DataAccessException;

@Singleton
public class UserAuthenticator {

	private final PasswordEncoder passwordEncoder;
	private final IDSLContextProvider dslContextProvider;

	/**
	 * Constructs an authenticator object keeps connection with the database and validate
	 * users at login attempt
	 *
	 * @param dslContextProvider The Domaiin Specific Language Providr for the database queries
	 * @throws NoSuchAlgorithmException
	 */
	public UserAuthenticator(
			IDSLContextProvider dslContextProvider,
			PasswordEncoder passwordEncoder) throws NoSuchAlgorithmException {

		this.dslContextProvider = dslContextProvider;
		this.passwordEncoder = passwordEncoder;
	}

	public Optional<UserRole> authorize(String username, String password) {
		Record3<byte[], byte[], UsersRole> record;
		try {
			record = this.dslContextProvider.get()
					.select(USERS.PASSWORD_DIGEST, USERS.PASSWORD_SALT, USERS.ROLE)
					.from(USERS)
					.where(USERS.USERNAME.eq(username))
					.fetchAny();
		} catch (DataAccessException e) {
			return Optional.empty();
		} catch (Exception ex) {
			return Optional.empty();
		}
		if (record == null) {
			return Optional.empty();
		}
		if (!this.authenticate(username, password, record)) {
			return Optional.empty();
		}

		UserRole role = UserRole.mapFromDB(record.component3());
		return Optional.of(role);
	}

	private boolean authenticate(String username, String password, Record3<byte[], byte[], UsersRole> record) {
		byte[] storedDigest = record.component1();
		byte[] storedSalt = record.component2();
		byte[] providedDigest = this.passwordEncoder.digest(password.getBytes(StandardCharsets.UTF_8), storedSalt);
		return Arrays.equals(storedDigest, providedDigest);
	}
}
