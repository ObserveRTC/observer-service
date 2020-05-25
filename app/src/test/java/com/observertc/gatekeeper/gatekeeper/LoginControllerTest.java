//package com.observertc.gatekeeper;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import com.nimbusds.jwt.JWTParser;
//import com.nimbusds.jwt.SignedJWT;
//import com.observertc.gatekeeper.model.UserRole;
//import com.observertc.gatekeeper.repositories.UserAuthenticator;
//import io.micronaut.context.annotation.Replaces;
//import io.micronaut.http.HttpRequest;
//import io.micronaut.http.HttpResponse;
//import io.micronaut.http.HttpStatus;
//import io.micronaut.http.client.DefaultHttpClient;
//import io.micronaut.http.client.DefaultHttpClientConfiguration;
//import io.micronaut.http.client.HttpClient;
//import io.micronaut.http.client.HttpClientConfiguration;
//import io.micronaut.http.client.exceptions.HttpClientResponseException;
//import io.micronaut.runtime.server.EmbeddedServer;
//import io.micronaut.security.authentication.UsernamePasswordCredentials;
//import io.micronaut.security.token.jwt.endpoints.TokenRefreshRequest;
//import io.micronaut.security.token.jwt.render.AccessRefreshToken;
//import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
//import io.micronaut.test.annotation.MicronautTest;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.security.NoSuchAlgorithmException;
//import java.text.ParseException;
//import java.time.Duration;
//import java.util.Optional;
//import javax.inject.Inject;
//import javax.inject.Singleton;
//import org.junit.jupiter.api.Test;
//
//@MicronautTest
//public class LoginControllerTest {
//
//	private static final String BASE = "/login";
//
//	@Inject
//	EmbeddedServer embeddedServer;
//
//	@Test
//	public void shouldFailAuthentication() throws MalformedURLException {
//		// Given
//		HttpClient client = makeClient();
//		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("invalid", "password");
//		HttpRequest request = HttpRequest.POST(BASE, credentials);
//
//		// Then
//		assertThrows(HttpClientResponseException.class, () ->
//				// When		
//				client.toBlocking().exchange(request, BearerAccessRefreshToken.class)
//		);
//	}
//
//	/**
//	 * Given
//	 * <p>
//	 * When
//	 *
//	 * @throws MalformedURLException
//	 * @throws ParseException
//	 */
//	@Test
//	public void shouldPassLogin() throws MalformedURLException, ParseException, InterruptedException {
//		// Given
//		HttpClient client = makeClient();
//		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("valid", "password");
//		HttpRequest request = HttpRequest.POST(BASE, credentials);
//
//		// When 1
//		HttpResponse<BearerAccessRefreshToken> response1 = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);
//
//		// Then 1
//		String accessToken = response1.body().getAccessToken();
//		String refreshToken = response1.body().getRefreshToken();
//		assertTrue(JWTParser.parse(accessToken) instanceof SignedJWT);
//		assertTrue(JWTParser.parse(refreshToken) instanceof SignedJWT);
//
//		// When 2
//		Thread.sleep(1000);
//		HttpResponse<AccessRefreshToken> response2 = client.toBlocking().exchange(HttpRequest.POST("/oauth/access_token",
//				new TokenRefreshRequest("refresh_token", refreshToken)), AccessRefreshToken.class);
//
//		// Then 2
//		assertEquals(response2.getStatus(), HttpStatus.OK);
//		assertNotEquals(accessToken, response2.body().getAccessToken());
//	}
//
//	private HttpClient makeClient() throws MalformedURLException {
//		HttpClientConfiguration configuration = new DefaultHttpClientConfiguration();
//		configuration.setReadTimeout(Duration.ofSeconds(10));
//		URL url = new URL("http://" + embeddedServer.getHost() + ":" + embeddedServer.getPort());
//		return new DefaultHttpClient(url, configuration);
//	}
//
//	/**
//	 * Mock the {@link UserAuthenticator} so the login endpoint can be controlled.
//	 */
//	@Singleton
//	@Replaces(UserAuthenticator.class)
//	public static class MockedUserAuthenticator extends UserAuthenticator {
//
//		public MockedUserAuthenticator() throws NoSuchAlgorithmException {
//			// Not important in this case
//			super(null, null);
//		}
//
//		@Override
//		public Optional<UserRole> authorize(String email, String password) {
//			return email.startsWith("valid") ? Optional.of(UserRole.ADMIN) : Optional.empty();
//		}
//	}
//}
