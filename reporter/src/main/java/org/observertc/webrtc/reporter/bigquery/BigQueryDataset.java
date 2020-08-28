package org.observertc.webrtc.reporter.bigquery;

import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryDataset {
	private static final Logger logger = LoggerFactory.getLogger(BigQueryDataset.class);

	private final BigQueryService bigQueryService;
	private final DatasetId datasetId;

	public BigQueryDataset(BigQueryService bigQueryService) {
		this.bigQueryService = bigQueryService;
		String projectName = this.bigQueryService.getProjectName();
		String datasetName = this.bigQueryService.getDatasetName();
		DatasetInfo datasetInfo = DatasetInfo.newBuilder(projectName, datasetName).build();
		DatasetId datasetId = datasetInfo.getDatasetId();
		this.datasetId = datasetId;
	}

	public Dataset getDataset() {
		return this.bigQueryService.getBigQuery().getDataset(datasetId);
	}

}