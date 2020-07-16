package org.observertc.webrtc.service.subscriptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceStartedEvent;
import javax.inject.Singleton;
import org.observertc.webrtc.common.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class ServiceStartedListener implements ApplicationEventListener<ServiceStartedEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceStartedListener.class);

	private final Job job;

	public ServiceStartedListener(KafkaInitializerJob kafkaInitializerJob) {

		this.job = new Job(ServiceStartedListener.class.getName())
				.withDescription("ServiceStartedListener job performs all task necessary to check at starrtup time.")
				.withTask(kafkaInitializerJob);
		;
		this.init();
	}


	@Override
	public void onApplicationEvent(ServiceStartedEvent event) {
		this.job.perform();
	}

	private void init() {

	}
}
