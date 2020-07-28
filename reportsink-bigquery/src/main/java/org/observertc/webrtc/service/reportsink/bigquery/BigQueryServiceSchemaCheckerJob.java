package org.observertc.webrtc.service.reportsink.bigquery;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import java.util.Map;
import org.observertc.webrtc.common.jobs.AbstractTask;
import org.observertc.webrtc.common.jobs.Job;
import org.observertc.webrtc.common.jobs.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryServiceSchemaCheckerJob extends Job {

	private static final Logger logger = LoggerFactory.getLogger(BigQueryServiceSchemaCheckerJob.class);
	private static final String CREATE_DATASET_TASK_NAME = "CreateDatasetTask";
	private static final String CREATE_INITIATED_CALL_TABLE_TASK_NAME = "CreateInitiatedCallsTableTask";
	private static final String CREATE_FINISHED_CALL_TABLE_TASK_NAME = "CreateFinishedCallsTableTask";
	private static final String CREATE_JOINED_PEER_CONNECTIONS_TABLE_TASK_NAME = "CreateJoinedPeerConnectionsTableTask";
	private static final String CREATE_DETACHED_PEER_CONNECTIONS_TABLE_TASK_NAME = "CreateDetachedPeerConnectionsTableTask";
	private static final String CREATE_OUTBOUND_REPORTS_TABLE_TASK_NAME = "CreateOutboundReportsTableTask";
	private static final String CREATE_INBOUND_REPORTS_TABLE_TASK_NAME = "CreateInboundReportsTableTask";
	private static final String CREATE_REMOTE_INBOUND_REPORTS_TABLE_TASK_NAME = "CreateRemoteInboundReportsTableTask";
	private static final String CREATE_REMOTE_INBOUND_RTP_SAMPLES_TABLE_TASK_NAME = "CreateRemoteInboundRTPSamplesTableTask";
	private static final String CREATE_OUTBOUND_RTP_SAMPLES_TABLE_TASK_NAME = "CreateOutboundRTPSamplesTableTask";
	private static final String CREATE_INBOUND_RTP_SAMPLES_TABLE_TASK_NAME = "CreateInboundRTPSamplesTableTask";

	private final BigQuery bigQuery;
	private final BigQueryReportServiceBuilder.Config config;

	public BigQueryServiceSchemaCheckerJob(BigQueryReportServiceBuilder.Config config) {
		this.bigQuery = BigQueryOptions.getDefaultInstance().getService();
		this.config = config;
		Task createDataset = this.makeCreateDatasetTask();
		Task createInitiatedCallsTable = this.makeCreateInitiatedCallsTableTask();
		Task createFinishedCallsTable = this.makeCreateFinishedCallsTableTask();
		Task createJoinedPeerConnectionsTable = this.makeJoinedPeerConnectionsTableTask();
		Task createDetachedPeerConnectionsTable = this.makeDetachedPeerConnectionsTableTask();
		Task createOutboundStreamSamplesTable = this.makeOutboundReportsTableTask();
		Task createInboundStreamSamplesTable = this.makeInboundReportsTableTask();
		Task createRemoteInboundStreamSamplesTable = this.makeRemoteInboundReportsTableTask();
		Task createRemoteInboundRTPSamplesTable = this.makeRemoteInboundRTPSamplesTableTask();
		Task createOutboundRTPSamplesTable = this.makeOutboundRTPSamplesTableTask();
		Task createInboundRTPSamplesTable = this.makeInboundRTPSamplesTableTask();
		this.withTask(createDataset)
				.withTask(createInitiatedCallsTable, createDataset)
				.withTask(createFinishedCallsTable, createDataset)
				.withTask(createJoinedPeerConnectionsTable, createDataset)
				.withTask(createDetachedPeerConnectionsTable, createDataset)
				.withTask(createOutboundStreamSamplesTable, createDataset)
				.withTask(createInboundStreamSamplesTable, createDataset)
				.withTask(createRemoteInboundStreamSamplesTable, createDataset)
				.withTask(createRemoteInboundRTPSamplesTable, createDataset)
				.withTask(createOutboundRTPSamplesTable, createDataset)
				.withTask(createInboundRTPSamplesTable, createDataset)
		;
	}

	private Task makeCreateDatasetTask() {
		return new AbstractTask(CREATE_DATASET_TASK_NAME) {
			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				if (!config.createDatasetIfNotExists) {
					return;
				}
				logger.info("Checking dataset {} existance in project {}", config.datasetName, config.projectName);
				DatasetId datasetId = DatasetId.of(config.projectName, config.datasetName);
				Dataset dataset = bigQuery.getDataset(datasetId);
				if (dataset != null && dataset.exists()) {
					return;
				}
				logger.info("Dataset {} does not exists, try to create it", config.datasetName);
				DatasetInfo datasetInfo = DatasetInfo.newBuilder(config.datasetName).build();
				Dataset newDataset = bigQuery.create(datasetInfo);
				String newDatasetName = newDataset.getDatasetId().getDataset();
				logger.info("BigQuery dataset {} created successfully", newDatasetName);
			}
		};
	}

	private Task makeCreateInitiatedCallsTableTask() {
		return new AbstractTask(CREATE_INITIATED_CALL_TABLE_TASK_NAME) {
			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.initiatedCallsTable);
				Schema schema = Schema.of(
						Field.newBuilder(InitiatedCallEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InitiatedCallEntry.CALL_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InitiatedCallEntry.INITIATED_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeCreateFinishedCallsTableTask() {
		return new AbstractTask(CREATE_FINISHED_CALL_TABLE_TASK_NAME) {
			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.finishedCallsTable);
				Schema schema = Schema.of(
						Field.newBuilder(FinishedCallEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(FinishedCallEntry.CALL_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(FinishedCallEntry.FINISHED_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeJoinedPeerConnectionsTableTask() {
		return new AbstractTask(CREATE_JOINED_PEER_CONNECTIONS_TABLE_TASK_NAME) {
			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.joinedPeerConnectionsTable);
				Schema schema = Schema.of(
						Field.newBuilder(JoinedPeerConnectionEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(JoinedPeerConnectionEntry.CALL_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(JoinedPeerConnectionEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(JoinedPeerConnectionEntry.JOINED_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeDetachedPeerConnectionsTableTask() {
		return new AbstractTask(CREATE_DETACHED_PEER_CONNECTIONS_TABLE_TASK_NAME) {
			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.detachedPeerConnectionsTable);
				Schema schema = Schema.of(
						Field.newBuilder(DetachedPeerConnectionEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(DetachedPeerConnectionEntry.CALL_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(DetachedPeerConnectionEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(DetachedPeerConnectionEntry.DETACHED_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private FieldList makeMediaStreamSampleRecordFieldList() {
		return FieldList.of(
				Field.newBuilder(MediaStreamReportEntryRecord.COUNT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
				,
				Field.newBuilder(MediaStreamReportEntryRecord.MINIMUM_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
				,
				Field.newBuilder(MediaStreamReportEntryRecord.MAXIMUM_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
				,
				Field.newBuilder(MediaStreamReportEntryRecord.SUM_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
		);
	}

	private Task makeOutboundReportsTableTask() {

		return new AbstractTask(CREATE_OUTBOUND_REPORTS_TABLE_TASK_NAME) {

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.outboundStreamReportsTable);
				Schema schema = Schema.of(
						Field.newBuilder(OutboundStreamReportEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamReportEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamReportEntry.SSRC_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamReportEntry.FIRST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamReportEntry.LAST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamReportEntry.BYTES_SENT_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamReportEntry.PACKETS_SENT_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeInboundReportsTableTask() {

		return new AbstractTask(CREATE_INBOUND_REPORTS_TABLE_TASK_NAME) {

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.inboundStreamReportsTable);
				Schema schema = Schema.of(
						Field.newBuilder(InboundStreamReportEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamReportEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamReportEntry.SSRC_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamReportEntry.FIRST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamReportEntry.LAST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamReportEntry.BYTES_RECEIVED_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamReportEntry.PACKETS_RECEIVED_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamReportEntry.PACKETS_LOST_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeRemoteInboundReportsTableTask() {

		return new AbstractTask(CREATE_REMOTE_INBOUND_REPORTS_TABLE_TASK_NAME) {

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.remoteInboundStreamReportsTable);
				Schema schema = Schema.of(
						Field.newBuilder(RemoteInboundStreamReportEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundStreamReportEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundStreamReportEntry.SSRC_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundStreamReportEntry.FIRST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundStreamReportEntry.LAST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundStreamReportEntry.RTT_IN_MS_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeRemoteInboundRTPSamplesTableTask() {

		return new AbstractTask(CREATE_REMOTE_INBOUND_RTP_SAMPLES_TABLE_TASK_NAME) {

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.remoteInboundRTPSamplesTable);
				Schema schema = Schema.of(
						Field.newBuilder(RemoteInboundRTPReportEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundRTPReportEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundRTPReportEntry.SSRC_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundRTPReportEntry.TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundRTPReportEntry.PACKETSLOST_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(RemoteInboundRTPReportEntry.RTT_IN_MS_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(RemoteInboundRTPReportEntry.JITTER_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(RemoteInboundRTPReportEntry.CODEC_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(RemoteInboundRTPReportEntry.MEDIA_TYPE_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()

				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeOutboundRTPSamplesTableTask() {

		return new AbstractTask(CREATE_OUTBOUND_RTP_SAMPLES_TABLE_TASK_NAME) {

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.outboundRTPSamplesTable);
				Schema schema = Schema.of(
						Field.newBuilder(OutboundRTPReportEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.SSRC_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.BYTES_SENT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.ENCODER_IMPLEMENTATION_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.FIR_COUNT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.FRAMES_ENCODED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.NACK_COUNT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.HEADER_BYTES_SENT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.KEYFRAMES_ENCODED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.MEDIA_TYPE_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.PACKETS_SENT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.PLI_COUNT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.QP_SUM_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.QUALITY_LIMITATION_REASON_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.QUALITY_LIMITATION_RESOLUTION_CHANGES_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.RETRANSMITTED_BYTES_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.RETRANSMITTED_PACKETS_SENT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.TOTAL_ENCODED_TIME_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.TOTAL_PACKET_SEND_DELAY_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(OutboundRTPReportEntry.TOTAL_ENCODED_BYTES_TARGET_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()

				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeInboundRTPSamplesTableTask() {

		return new AbstractTask(CREATE_INBOUND_RTP_SAMPLES_TABLE_TASK_NAME) {

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.inboundRTPSamplesTable);
				Schema schema = Schema.of(
						Field.newBuilder(InboundRTPReportEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundRTPReportEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundRTPReportEntry.SSRC_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundRTPReportEntry.TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundRTPReportEntry.BYTES_RECEIVED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.DECODER_IMPLEMENTATION_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.FIR_COUNT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.FRAMES_DECODED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.NACK_COUNT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.HEADER_BYTES_RECEIVED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.KEYFRAMES_DECODED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.MEDIA_TYPE_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.PACKETS_RECEIVED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.PLI_COUNT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.QP_SUM_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.JITTER_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.TOTAL_DECODE_TIME_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.TOTAL_INTERFRAME_DELAY_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.TOTAL_SQUARED_INITER_FREAME_DELAY_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.PACKETS_LOST_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.ESTIMATED_PLAYOUT_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.FEC_PACKETS_DISCARDED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.LAST_PACKET_RECEIVED_TIMESTAMP, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(InboundRTPReportEntry.FEC_PACKETS_RECEIVED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()

				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}


	private void createTableIfNotExists(TableId tableId, Schema schema) {
		logger.info("Checking table {} existance in dataset: {}, project: {}", tableId.getTable(), tableId.getDataset(), tableId.getProject());
		Table table = bigQuery.getTable(tableId);
		if (table != null && table.exists()) {
			return;
		}
		if (!this.config.createDatasetIfNotExists) {
			return;
		}
		try {
			TableDefinition tableDefinition = StandardTableDefinition.of(schema);
			TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();
			bigQuery.create(tableInfo);
			logger.info("Table {} is succcessfully created", tableId.getTable());
		} catch (BigQueryException e) {
			logger.error("Error during table creation. Table: {}", tableId.getTable());
		}
	}
}
