package org.observertc.webrtc.service.subscriptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import java.util.Map;
import javax.inject.Singleton;
import org.observertc.webrtc.common.jobs.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class DatabaseSchemaCheckerJob extends AbstractTask {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaCheckerJob.class);

	public DatabaseSchemaCheckerJob() {
		super(DatabaseSchemaCheckerJob.class.getName());
	}

	@Override
	protected void onExecution(Map<String, Map<String, Object>> results) {

	}

}
