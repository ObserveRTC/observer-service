package com.observertc.gatekeeper.webrtcstat.bigquery;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import javax.inject.Singleton;

@Singleton
public class BigQueryService {
	public static final String PROJECT_NAME = "observertc";
	private BigQuery bigQuery;

	public BigQueryService() {
		this.bigQuery = BigQueryOptions.getDefaultInstance().getService();
	}

	public BigQuery getBigQuery() {
		return this.bigQuery;
	}
}
