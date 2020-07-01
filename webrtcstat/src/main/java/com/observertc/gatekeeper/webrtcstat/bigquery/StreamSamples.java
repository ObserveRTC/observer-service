package com.observertc.gatekeeper.webrtcstat.bigquery;

import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.TableId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StreamSamples {
	private static final Logger logger = LoggerFactory.getLogger(JoinedPeerConnections.class);

	private static final String TABLE_NAME = "StreamSamples";
	private final WebRTCDataSet webRTCDataSet;
	private final BigQueryService bigQueryService;

	public StreamSamples(WebRTCDataSet webRTCDataSet, BigQueryService bigQueryService) {
		this.webRTCDataSet = webRTCDataSet;
		this.bigQueryService = bigQueryService;
	}

	public void insert(StreamSample streamSample) {
		TableId tableId = this.getTableId();

		InsertAllResponse response =
				this.bigQueryService.getBigQuery().insertAll(
						InsertAllRequest.newBuilder(tableId)
								.addRow(streamSample.toMap())
								.build());
		if (response.hasErrors()) {
			// If any of the insertions failed, this lets you inspect the errors
			for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
				logger.error("{}: {}", entry.getKey(), String.join(", \n",
						entry.getValue().stream().map(Object::toString).collect(Collectors.toList())));
				// inspect row error
			}
		}
	}

	private TableId getTableId() {
		return TableId.of(BigQueryService.PROJECT_NAME, WebRTCDataSet.DATASET_NAME, TABLE_NAME);
	}

}
