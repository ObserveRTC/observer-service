package org.observertc.webrtc.observer.service.gatekeeper;

import org.observertc.webrtc.observer.service.repositories.ObserverRepository;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import javax.inject.Inject;

@MicronautTest
public class ObserverControllerTest {

	private static final String BASE = "/observers";

	@Inject
	ObserverRepository observerRepository;

	@Inject
	EmbeddedServer embeddedServer;

//	@Test
//	public void testIndex() throws Exception {
//		try (RxHttpClient client = embeddedServer.getApplicationContext().createBean(RxHttpClient.class, embeddedServer.getURL())) {
//			assertEquals(HttpStatus.OK, client.toBlocking().exchange(BASE).status());
//		}
//	}
//
//	private HttpClient makeClient() throws MalformedURLException {
//		HttpClientConfiguration configuration = new DefaultHttpClientConfiguration();
//		configuration.setReadTimeout(Duration.ofSeconds(10));
//		URL url = new URL("http://" + embeddedServer.getHost() + ":" + embeddedServer.getPort());
//		return new DefaultHttpClient(url, configuration);
//	}
}
