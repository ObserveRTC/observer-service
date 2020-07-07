package org.observertc.webrtc.service.reportsink.bigquery;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import javax.inject.Singleton;

@Singleton
public class BigQueryService {
	private BigQuery bigQuery;

	public BigQueryService(String projectName, String datasetName) {
		this.bigQuery = BigQueryOptions.getDefaultInstance().getService();
	}

	public BigQuery getBigQuery() {
		return this.bigQuery;
	}

	public String getProjectName() {
		return "observertc";
	}

	public String getDatasetName() {
		return "WebRTC";
	}

}
