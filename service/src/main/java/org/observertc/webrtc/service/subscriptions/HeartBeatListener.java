package org.observertc.webrtc.service.subscriptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.health.HeartbeatEvent;
import java.time.LocalDateTime;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.observertc.webrtc.common.jobs.AbstractTask;
import org.observertc.webrtc.common.jobs.Job;
import org.observertc.webrtc.common.jobs.Task;
import org.observertc.webrtc.service.ApplicationTimeZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class HeartBeatListener implements ApplicationEventListener<HeartbeatEvent> {
	private static final Logger logger = LoggerFactory.getLogger(HeartBeatListener.class);

	@Inject
	ApplicationTimeZoneId applicationTimeZoneId;

	private final Job job;

	public HeartBeatListener(KafkaInitializerJob kafkaInitializerJob) {

		this.job = new Job(HeartBeatListener.class.getName())
				.withDescription("HeartBeatListener job is to perform tasks regularly during service lifetime");
		;
	}

	public HeartBeatListener scheduleTask(int periodInS, String name, Runnable action) {
		Task task = new AbstractTask(name) {
			private LocalDateTime lastExecuted = null;

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				LocalDateTime now = LocalDateTime.now(applicationTimeZoneId.getZoneId());
				if (lastExecuted != null &&
						now.minusSeconds(periodInS).compareTo(lastExecuted) < 0) {
					return;
				}
				action.run();
				this.reset();
			}
		};
		this.job.withTask(task);
		return this;
	}

	@Override
	public void onApplicationEvent(HeartbeatEvent event) {
		this.job.perform();
	}
}
