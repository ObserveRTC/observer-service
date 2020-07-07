package org.observertc.webrtc.service.reportsink.bigquery;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import javax.inject.Singleton;

@Singleton
public class BigQueryService {
	private final String projectName;
	private final String datasetName;
	private BigQuery bigQuery;

	public BigQueryService(String projectName, String datasetName) {
		this.bigQuery = BigQueryOptions.getDefaultInstance().getService();
		this.projectName = projectName;
		this.datasetName = datasetName;

	}

	public BigQuery getBigQuery() {
		return this.bigQuery;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public String getDatasetName() {
		return this.datasetName;
	}

}
