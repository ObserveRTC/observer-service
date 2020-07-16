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
	private static final String CREATE_OUTBOUND_STREAM_SAMPLES_TABLE_TASK_NAME = "CreateOutboundStreamSamplesTableTask";
	private static final String CREATE_INBOUND_STREAM_SAMPLES_TABLE_TASK_NAME = "CreateInboundStreamSamplesTableTask";
	private static final String CREATE_REMOTE_INBOUND_STREAM_SAMPLES_TABLE_TASK_NAME = "CreateRemoteInboundStreamSamplesTableTask";


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
		Task createOutboundStreamSamplesTable = this.makeOutboundStreamSamplesTableTask();
		Task createInboundStreamSamplesTable = this.makeInboundStreamSamplesTableTask();
		Task createRemoteInboundStreamSamplesTable = this.makeRemoteInboundStreamSamplesTableTask();
		this.withTask(createDataset)
				.withTask(createInitiatedCallsTable, createDataset)
				.withTask(createFinishedCallsTable, createDataset)
				.withTask(createJoinedPeerConnectionsTable, createDataset)
				.withTask(createDetachedPeerConnectionsTable, createDataset)
				.withTask(createOutboundStreamSamplesTable, createDataset)
				.withTask(createInboundStreamSamplesTable, createDataset)
				.withTask(createRemoteInboundStreamSamplesTable, createDataset)

		;
	}

	private Task makeCreateDatasetTask() {
		return new AbstractTask(CREATE_DATASET_TASK_NAME) {
			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
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
				Field.newBuilder(MediaStreamSampleEntryRecord.COUNT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
				,
				Field.newBuilder(MediaStreamSampleEntryRecord.MINIMUM_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
				,
				Field.newBuilder(MediaStreamSampleEntryRecord.MAXIMUM_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
				,
				Field.newBuilder(MediaStreamSampleEntryRecord.SUM_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
		);
	}

	private Task makeOutboundStreamSamplesTableTask() {

		return new AbstractTask(CREATE_OUTBOUND_STREAM_SAMPLES_TABLE_TASK_NAME) {

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.outboundStreamSamplesTable);
				Schema schema = Schema.of(
						Field.newBuilder(OutboundStreamSampleEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamSampleEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamSampleEntry.SSRC_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamSampleEntry.FIRST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamSampleEntry.LAST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamSampleEntry.BYTES_SENT_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(OutboundStreamSampleEntry.PACKETS_SENT_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeInboundStreamSamplesTableTask() {

		return new AbstractTask(CREATE_INBOUND_STREAM_SAMPLES_TABLE_TASK_NAME) {

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.inboundStreamSamplesTable);
				Schema schema = Schema.of(
						Field.newBuilder(InboundStreamSampleEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamSampleEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamSampleEntry.SSRC_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamSampleEntry.FIRST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamSampleEntry.LAST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamSampleEntry.BYTES_RECEIVED_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamSampleEntry.PACKETS_RECEIVED_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(InboundStreamSampleEntry.PACKETS_LOST_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeRemoteInboundStreamSamplesTableTask() {

		return new AbstractTask(CREATE_REMOTE_INBOUND_STREAM_SAMPLES_TABLE_TASK_NAME) {

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.remoteInboundStreamSamplesTable);
				Schema schema = Schema.of(
						Field.newBuilder(RemoteInboundStreamSampleEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundStreamSampleEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundStreamSampleEntry.SSRC_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundStreamSampleEntry.FIRST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundStreamSampleEntry.LAST_SAMPLE_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(RemoteInboundStreamSampleEntry.RTT_IN_MS_FIELD_NAME, LegacySQLTypeName.RECORD,
								makeMediaStreamSampleRecordFieldList()).setMode(Field.Mode.REQUIRED).build()
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
