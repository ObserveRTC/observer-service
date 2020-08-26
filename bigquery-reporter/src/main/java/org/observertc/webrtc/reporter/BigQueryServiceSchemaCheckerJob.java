package org.observertc.webrtc.reporter;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
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
	private static final String CREATE_REMOTE_INBOUND_RTP_SAMPLES_TABLE_TASK_NAME = "CreateRemoteInboundRTPSamplesTableTask";
	private static final String CREATE_OUTBOUND_RTP_SAMPLES_TABLE_TASK_NAME = "CreateOutboundRTPSamplesTableTask";
	private static final String CREATE_INBOUND_RTP_SAMPLES_TABLE_TASK_NAME = "CreateInboundRTPSamplesTableTask";
	private static final String CREATE_ICE_CANDIDATE_PAIRS_TABLE_TASK_NAME = "CreateICECandidatePairsTableTask";
	private static final String CREATE_ICE_LOCAL_CANDIDATE_TABLE_TASK_NAME = "CreateICELocalCandidatesTableTask";
	private static final String CREATE_ICE_REMOTE_CANDIDATE_TABLE_TASK_NAME = "CreateICERemoteCandidatesTableTask";
	private static final String CREATE_MEDIA_SOURCES_TABLE_TASK_NAME = "CreateMediaSourcesTableTask";
	private static final String CREATE_TRACK_REPORTS_TABLE_TASK_NAME = "CreateTrackReportsTableTask";

	private static volatile boolean run = false;

	private final BigQuery bigQuery;
	private final ReporterConfig config;

	public BigQueryServiceSchemaCheckerJob(ReporterConfig config) {
		this.bigQuery = BigQueryOptions.getDefaultInstance().getService();
		this.config = config;
		Task createDataset = this.makeCreateDatasetTask();
		Task createInitiatedCallsTable = this.makeCreateInitiatedCallsTableTask();
		Task createFinishedCallsTable = this.makeCreateFinishedCallsTableTask();
		Task createJoinedPeerConnectionsTable = this.makeJoinedPeerConnectionsTableTask();
		Task createDetachedPeerConnectionsTable = this.makeDetachedPeerConnectionsTableTask();
		Task createRemoteInboundRTPSamplesTable = this.makeRemoteInboundRTPSamplesTableTask();
		Task createOutboundRTPSamplesTable = this.makeOutboundRTPSamplesTableTask();
		Task createInboundRTPSamplesTable = this.makeInboundRTPSamplesTableTask();
		Task createICECandidatePairsTable = this.makeICECandidatePairsTableTask();
		Task createICELocalCandidates = this.makeICELocalCandidateTableTask();
		Task createICERemoteCandidates = this.makeICERemoteCandidateTableTask();
		Task createMediaSources = this.makeMediaSourcesTableTask();
		Task createTrackReports = this.makeTrackReportsTableTask();
		this.withTask(createDataset)
				.withTask(createInitiatedCallsTable, createDataset)
				.withTask(createFinishedCallsTable, createDataset)
				.withTask(createJoinedPeerConnectionsTable, createDataset)
				.withTask(createDetachedPeerConnectionsTable, createDataset)
				.withTask(createRemoteInboundRTPSamplesTable, createDataset)
				.withTask(createOutboundRTPSamplesTable, createDataset)
				.withTask(createInboundRTPSamplesTable, createDataset)
				.withTask(createICECandidatePairsTable, createDataset)
				.withTask(createICELocalCandidates, createDataset)
				.withTask(createICERemoteCandidates, createDataset)
				.withTask(createMediaSources, createDataset)
				.withTask(createTrackReports, createDataset)
		;
	}

	@Override
	public void perform() {
		if (run) {
			return;
		}
		run = true;
		super.perform();
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
						Field.newBuilder(JoinedPeerConnectionEntry.BROWSERID_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
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
						Field.newBuilder(DetachedPeerConnectionEntry.BROWSERID_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(DetachedPeerConnectionEntry.DETACHED_TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeICECandidatePairsTableTask() {
		return new AbstractTask(CREATE_ICE_CANDIDATE_PAIRS_TABLE_TASK_NAME) {
			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.iceCandidatePairsTable);
				Schema schema = Schema.of(
						Field.newBuilder(ICECandidatePairEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.CANDIDATE_ID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.WRITABLE_FIELD_NAME, LegacySQLTypeName.BOOLEAN).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.TOTAL_ROUND_TRIP_TIME_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.STATE_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.NOMINATED_FIELD_NAME, LegacySQLTypeName.BOOLEAN).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.AVAILABLE_OUTGOING_BITRATE_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.BYTES_RECEIVED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.BYTES_SENT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.CONSENT_REQUESTS_SENT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.CURRENT_ROUND_TRIP_TIME_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.PRIORITY_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.REQUESTS_RECEIVED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.REQUESTS_SENT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.RESPONSES_RECEIVED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICECandidatePairEntry.RESPONSES_SENT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.REQUIRED).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeICELocalCandidateTableTask() {
		return new AbstractTask(CREATE_ICE_LOCAL_CANDIDATE_TABLE_TASK_NAME) {
			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.iceLocalCandidatesTable);
				Schema schema = Schema.of(
						Field.newBuilder(ICELocalCandidateEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.CANDIDATE_ID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.DELETED_FIELD_NAME, LegacySQLTypeName.BOOLEAN).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.CANDIDATE_TYPE_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.PORT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.IP_LSH_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.PRIORITY_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.NETWORK_TYPE_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.PROTOCOL_TYPE_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.IP_FLAG_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}


	private Task makeICERemoteCandidateTableTask() {
		return new AbstractTask(CREATE_ICE_REMOTE_CANDIDATE_TABLE_TASK_NAME) {
			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.iceRemoteCandidatesTable);
				Schema schema = Schema.of(
						Field.newBuilder(ICERemoteCandidateEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICERemoteCandidateEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICERemoteCandidateEntry.CANDIDATE_ID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICERemoteCandidateEntry.TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(ICELocalCandidateEntry.CANDIDATE_TYPE_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICERemoteCandidateEntry.DELETED_FIELD_NAME, LegacySQLTypeName.BOOLEAN).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICERemoteCandidateEntry.PORT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICERemoteCandidateEntry.IP_LSH_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICERemoteCandidateEntry.PRIORITY_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICERemoteCandidateEntry.PROTOCOL_TYPE_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(ICERemoteCandidateEntry.IP_FLAG_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}


	private Task makeMediaSourcesTableTask() {

		return new AbstractTask(CREATE_MEDIA_SOURCES_TABLE_TASK_NAME) {

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.mediaSourcesTable);
				Schema schema = Schema.of(
						Field.newBuilder(MediaSourceEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(MediaSourceEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(MediaSourceEntry.TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(MediaSourceEntry.MEDIA_SOURCE_ID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(MediaSourceEntry.FRAMES_PER_SECOND_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(MediaSourceEntry.HEIGHT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(MediaSourceEntry.WIDTH_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(MediaSourceEntry.AUDIO_LEVEL_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(MediaSourceEntry.MEDIA_TYPE_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(MediaSourceEntry.TOTAL_AUDIO_ENERGY_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(MediaSourceEntry.TOTAL_SAMPLES_DURATION_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
				);
				createTableIfNotExists(tableId, schema);
			}
		};
	}

	private Task makeTrackReportsTableTask() {

		return new AbstractTask(CREATE_TRACK_REPORTS_TABLE_TASK_NAME) {

			@Override
			protected void onExecution(Map<String, Map<String, Object>> results) {
				TableId tableId = TableId.of(config.projectName, config.datasetName, config.trackReportsTable);
				Schema schema = Schema.of(
						Field.newBuilder(TrackReportEntry.OBSERVER_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(TrackReportEntry.PEER_CONNECTION_UUID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(TrackReportEntry.TIMESTAMP_FIELD_NAME, LegacySQLTypeName.TIMESTAMP).setMode(Field.Mode.REQUIRED).build()
						,
						Field.newBuilder(TrackReportEntry.TRACK_ID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.CONCEALED_SAMPLES_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.TOTAL_SAMPLES_RECEIVED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.SILENT_CONCEALED_SAMPLES_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.REMOVED_SAMPLES_FOR_ACCELERATION_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.AUDIO_LEVEL_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.MEDIA_TYPE_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.TOTAL_AUDIO_ENERGY_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.TOTAL_SAMPLES_DURATION_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.REMOTE_SOURCE_FIELD_NAME, LegacySQLTypeName.BOOLEAN).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.JITTER_BUFFER_EMITTED_COUNT_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.JITTER_BUFFER_DELAY_FIELD_NAME, LegacySQLTypeName.FLOAT).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.INSERTED_SAMPLES_FOR_DECELERATION_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.HUGE_FRAMES_SENT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.FRAMES_WIDTH_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.FRAMES_SENT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.FRAMES_RECEIVED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.FRAMES_DROPPED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.FRAMES_DECODED_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.FRAMES_HEIGHT_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.ENDED_FIELD_NAME, LegacySQLTypeName.BOOLEAN).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.DETACHED_FIELD_NAME, LegacySQLTypeName.BOOLEAN).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.CONCEALMENT_EVENTS_FIELD_NAME, LegacySQLTypeName.INTEGER).setMode(Field.Mode.NULLABLE).build()
						,
						Field.newBuilder(TrackReportEntry.MEDIA_SOURCE_ID_FIELD_NAME, LegacySQLTypeName.STRING).setMode(Field.Mode.NULLABLE).build()
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
		if (!this.config.createTableIfNotExists) {
			return;
		}
		try {
			TableDefinition tableDefinition = StandardTableDefinition.of(schema);
			TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();
			bigQuery.create(tableInfo);
			logger.info("Table {} is succcessfully created", tableId.getTable());
		} catch (BigQueryException e) {
			logger.error("Error during table creation. Table: " + tableId.getTable(), e);
		}
	}
}
