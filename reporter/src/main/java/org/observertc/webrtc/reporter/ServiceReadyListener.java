package org.observertc.webrtc.reporter;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceReadyEvent;
import javax.inject.Singleton;
import org.observertc.webrtc.common.jobs.Job;
import org.observertc.webrtc.reporter.bigquery.BigQueryServiceSchemaCheckerJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class ServiceReadyListener implements ApplicationEventListener<ServiceReadyEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceReadyListener.class);

	private Job job;

	public ServiceReadyListener(ReporterConfig config,
								BigQueryServiceSchemaCheckerJob bigQueryServiceSchemaCheckerJob) {
		ConfigProfile profile = ConfigProfile.fromProfile(config.profile);
		new ConfigProfileProcessor() {
			@Override
			public void actionOnBigQuery() {
				job = bigQueryServiceSchemaCheckerJob;
			}
		}.accept(profile);
		this.job = bigQueryServiceSchemaCheckerJob;
		this.init();
	}


	private void init() {

	}

	@Override
	public void onApplicationEvent(ServiceReadyEvent event) {
		this.job.perform();
	}
}
