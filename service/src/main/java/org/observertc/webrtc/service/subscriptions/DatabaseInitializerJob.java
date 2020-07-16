package org.observertc.webrtc.service.subscriptions;

import javax.inject.Singleton;
import org.observertc.webrtc.common.jobs.Job;
import org.observertc.webrtc.service.repositories.IDSLContextProvider;

@Singleton
public class DatabaseInitializerJob extends Job {

	private IDSLContextProvider contextProvider;

	public DatabaseInitializerJob(IDSLContextProvider contextProvider) {
		this.contextProvider = contextProvider;
	}


}
