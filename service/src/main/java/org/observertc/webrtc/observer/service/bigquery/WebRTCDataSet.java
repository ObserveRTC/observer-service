package org.observertc.webrtc.observer.service.bigquery;

import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WebRTCDataSet {
	private static final Logger logger = LoggerFactory.getLogger(WebRTCDataSet.class);

	public static final String DATASET_NAME = "WebRTC";
	private final BigQueryService bigQueryService;
	private final DatasetId datasetId;

	public WebRTCDataSet(BigQueryService bigQueryService) {
		this.bigQueryService = bigQueryService;
		DatasetInfo datasetInfo = DatasetInfo.newBuilder(BigQueryService.PROJECT_NAME, DATASET_NAME).build();
		DatasetId datasetId = datasetInfo.getDatasetId();
		this.datasetId = datasetId;
	}

	public Dataset getDataset() {
		return this.bigQueryService.getBigQuery().getDataset(datasetId);
	}

}