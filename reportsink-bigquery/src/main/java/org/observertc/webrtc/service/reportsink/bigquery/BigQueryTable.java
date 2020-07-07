package org.observertc.webrtc.service.reportsink.bigquery;

import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.TableId;
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

	public void insert(T entry) {
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