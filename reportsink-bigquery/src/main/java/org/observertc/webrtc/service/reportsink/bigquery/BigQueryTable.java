package org.observertc.webrtc.service.reportsink.bigquery;

import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.TableId;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryTable<T extends BigQueryEntry> {
	private static final Logger logger = LoggerFactory.getLogger(BigQueryTable.class);

	private final String tableName;
	private final BigQueryService bigQueryService;

	public BigQueryTable(BigQueryService bigQueryService,
						 String tableName
	) {
		this.tableName = tableName;
		this.bigQueryService = bigQueryService;
	}

	private void logEntry(T entry) {
		String rowValue = this.mapString(entry.toMap(), "\t");
		logger.info("project: {}, dataset: {}, table {}. The row: \n {}",
				this.bigQueryService.getProjectName(), this.bigQueryService.getDatasetName(), this.tableName, rowValue);
	}

	private String mapString(Map<String, Object> map, String prefix) {

		StringBuffer resultBuffer = new StringBuffer();
		Iterator<Map.Entry<String, Object>> mapIt = map.entrySet().iterator();
		for (; mapIt.hasNext(); ) {
			Map.Entry<String, Object> entry = mapIt.next();
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value == null) {
				resultBuffer.append(String.format("%s%s: null\n", prefix, key));
			} else if (value instanceof Map) {
				resultBuffer.append(String.format("%s%s: %s", prefix, key,
						this.mapString((Map<String, Object>) value, prefix + "\t")));
			} else {
				resultBuffer.append(String.format("%s%s: %s\n", prefix, entry.getKey(), value.toString()));
			}
		}
		return resultBuffer.toString();
	}

	public void insert(T entry) {
		this.logEntry(entry);
		TableId tableId = this.getTableId();

		InsertAllResponse response =
				this.bigQueryService.getBigQuery().insertAll(
						InsertAllRequest.newBuilder(tableId)
								.addRow(entry.toMap())
								.build());
		if (response.hasErrors()) {
			// If any of the insertions failed, this lets you inspect the errors
			for (Map.Entry<Long, List<BigQueryError>> errorEntry : response.getInsertErrors().entrySet()) {
				logger.error("{}: {}", errorEntry.getKey(), String.join(", \n",
						errorEntry.getValue().stream().map(Object::toString).collect(Collectors.toList())));
				// inspect row error
			}
		}
	}

	private TableId getTableId() {
		String projectName = this.bigQueryService.getProjectName();
		String datasetName = this.bigQueryService.getDatasetName();
		return TableId.of(projectName, datasetName, this.tableName);
	}

}