package org.observertc.webrtc.reporter.bigquery;

import java.util.Map;

public interface BigQueryEntry {

	Map<String, Object> toMap();

}
