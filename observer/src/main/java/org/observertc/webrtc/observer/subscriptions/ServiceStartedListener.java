package org.observertc.webrtc.observer.subscriptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceReadyEvent;
import javax.inject.Singleton;
import org.observertc.webrtc.common.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class ServiceStartedListener implements ApplicationEventListener<ServiceReadyEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceStartedListener.class);

	private final Job job;

	public ServiceStartedListener() {
		this.job = null;
	}


	private void init() {

	}

	@Override
	public void onApplicationEvent(ServiceReadyEvent event) {

	}
}
