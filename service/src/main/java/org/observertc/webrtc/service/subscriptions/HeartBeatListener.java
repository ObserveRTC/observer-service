package org.observertc.webrtc.service.subscriptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.health.HeartbeatEvent;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.observertc.webrtc.service.ObserverTimeZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class HeartBeatListener implements ApplicationEventListener<HeartbeatEvent> {
	private static final Logger logger = LoggerFactory.getLogger(HeartBeatListener.class);

	@Inject
	ObserverTimeZoneId observerTimeZoneId;

	public HeartBeatListener(KafkaInitializerJob kafkaInitializerJob) {

	}

	public HeartBeatListener scheduleTask(int periodInS, String name, Runnable action) {
		return this;
	}

	@Override
	public void onApplicationEvent(HeartbeatEvent event) {
		
	}
}
