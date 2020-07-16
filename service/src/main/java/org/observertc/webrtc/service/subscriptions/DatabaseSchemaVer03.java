package org.observertc.webrtc.service.subscriptions;

import java.util.Map;
import org.jooq.DSLContext;
import org.observertc.webrtc.common.jobs.AbstractTask;
import org.observertc.webrtc.service.repositories.IDSLContextProvider;

public class DatabaseSchemaVer03 extends AbstractTask {
	private final IDSLContextProvider contextProvider;

	public DatabaseSchemaVer03(IDSLContextProvider contextProvider) {
		super(DatabaseSchemaVer03.class.getName());
		this.contextProvider = contextProvider;
	}

	@Override
	protected void onExecution(Map<String, Map<String, Object>> results) {
		DSLContext context = this.contextProvider.get();
	}

}
