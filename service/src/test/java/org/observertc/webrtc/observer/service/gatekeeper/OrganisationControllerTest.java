package org.observertc.webrtc.observer.service.gatekeeper;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import javax.inject.Inject;

@MicronautTest
public class OrganisationControllerTest {

	private static final String BASE = "/organisations";

	@Inject
	EmbeddedServer embeddedServer;

//	@Test
//	public void testIndex() throws Exception {
//		try (RxHttpClient client = embeddedServer.getApplicationContext().createBean(RxHttpClient.class, embeddedServer.getURL())) {
//			assertEquals(HttpStatus.OK, client.toBlocking().exchange(URL).status());
//		}
//	}
}
