package org.observertc.webrtc.common.jobs;

import java.util.Map;

public interface Task {
	String getName();

	String getDescription();

	void execute(Map<String, Map<String, Object>> results);

	Map<String, Object> getResults();
}
