package org.observertc.webrtc.observer.service.initializers;

import io.micronaut.configuration.kafka.config.KafkaDefaultConfiguration;
import javax.inject.Singleton;
import org.apache.kafka.clients.admin.AdminClient;

@Singleton
public class KafkaInitializer implements Runnable {
	private final AdminClient adminClient;

	public KafkaInitializer(KafkaDefaultConfiguration configuration) {
		this.adminClient = AdminClient.create(configuration.getConfig());
	}

	@Override
	public void run() {
		
	}

}
