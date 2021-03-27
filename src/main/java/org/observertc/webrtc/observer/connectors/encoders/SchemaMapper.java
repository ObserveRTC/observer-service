package org.observertc.webrtc.observer.connectors.encoders;

import org.apache.avro.Schema;
import org.observertc.webrtc.observer.common.TaskAbstract;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SchemaMapper extends TaskAbstract<Map<ReportType, ReportMapper>> {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(SchemaMapper.class);

    private final Map<ReportType, Schema> schemaMap = new HashMap<>();
    protected Logger logger = DEFAULT_LOGGER;

    public SchemaMapper() {
        this.schemaMap.put(ReportType.INITIATED_CALL, InitiatedCall.getClassSchema());
        this.schemaMap.put(ReportType.FINISHED_CALL, FinishedCall.getClassSchema());
        this.schemaMap.put(ReportType.JOINED_PEER_CONNECTION, JoinedPeerConnection.getClassSchema());
        this.schemaMap.put(ReportType.DETACHED_PEER_CONNECTION, DetachedPeerConnection.getClassSchema());
        this.schemaMap.put(ReportType.OBSERVER_EVENT, ObserverEventReport.getClassSchema());
        this.schemaMap.put(ReportType.INBOUND_RTP, InboundRTP.getClassSchema());
        this.schemaMap.put(ReportType.OUTBOUND_RTP, OutboundRTP.getClassSchema());
        this.schemaMap.put(ReportType.REMOTE_INBOUND_RTP, RemoteInboundRTP.getClassSchema());
        this.schemaMap.put(ReportType.ICE_CANDIDATE_PAIR, ICECandidatePair.getClassSchema());
        this.schemaMap.put(ReportType.ICE_LOCAL_CANDIDATE, ICELocalCandidate.getClassSchema());
        this.schemaMap.put(ReportType.ICE_REMOTE_CANDIDATE, ICERemoteCandidate.getClassSchema());
        this.schemaMap.put(ReportType.TRACK, Track.getClassSchema());
        this.schemaMap.put(ReportType.MEDIA_SOURCE, MediaSource.getClassSchema());
        this.schemaMap.put(ReportType.USER_MEDIA_ERROR, UserMediaError.getClassSchema());
        this.schemaMap.put(ReportType.MEDIA_DEVICE, MediaDevice.getClassSchema());
        this.schemaMap.put(ReportType.CLIENT_DETAILS, ClientDetails.getClassSchema());
        this.schemaMap.put(ReportType.EXTENSION, ExtensionReport.getClassSchema());
    }

    @Override
    protected Map<ReportType, ReportMapper> perform() throws Throwable {
        Iterator<Map.Entry<ReportType, Schema>> it = this.schemaMap.entrySet().iterator();
        Map<ReportType, ReportMapper> result = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry<ReportType, Schema> entry = it.next();
            ReportType reportType = entry.getKey();
            Schema schema = entry.getValue();
            ReportMapper reportMapper = this.makeReportMapper(reportType, schema);
            result.put(reportType, reportMapper);
        }
        return result;
    }

    protected ReportMapper makeReportMapper(ReportType reportType, Schema schema) {
        ReportMapper result = new ReportMapper();
        // common fields
        result
                .add("timestamp", Function.identity(), Function.identity())
                .add("marker", Function.identity(), Function.identity())
                .add("type", Function.identity(), Function.identity())
        ;
        ReportMapper payloadMapper = new ReportMapper();
        this.schemaFieldWalker(schema, fieldInfo -> {
            // excluding fields:
//            if (List.of("version", "type")
//                    .stream()
//                    .anyMatch(f -> f.equals(fieldInfo.fieldName)))
//            {
//                return;
//            }
            payloadMapper.add(fieldInfo.fieldName, Function.identity(), Function.identity());
        });
        result.add("payload", payloadMapper);
        return result;
    }

    public SchemaMapper withLogger(Logger logger) {
        this.logger.info("Default logger for {} is switched to {}", this.getClass().getSimpleName(), logger.getName());
        this.logger = logger;
        return this;
    }

    protected void schemaFieldWalker(Schema schema, Consumer<FieldInfo> evaluator) {
        for (Schema.Field field : schema.getFields()) {
            String fieldName = field.name();
            Schema fieldSchema = field.schema();
            Schema.Type fieldType = fieldSchema.getType();
            if (fieldType.equals(Schema.Type.UNION) && fieldSchema.getTypes().size() == 2) {
                // these are most likely types of the avro schema where null union with the actual type
                Schema subSchema = fieldSchema.getTypes().get(0);
                if (subSchema.getType().equals(Schema.Type.NULL)) { // most likely nullable
                    subSchema = fieldSchema.getTypes().get(1);
                }
                FieldInfo fieldInfo = new FieldInfo(subSchema, true, fieldName);
                evaluator.accept(fieldInfo);
            } else {
                FieldInfo fieldInfo = new FieldInfo(fieldSchema, true, fieldName);
                evaluator.accept(fieldInfo);
            }
        }
    }

    protected class FieldInfo {
        public final Schema schema;
        public final boolean embedded;
        public final String fieldName;

        FieldInfo(Schema schema, boolean embedded, String fieldName) {
            this.schema = schema;
            this.embedded = embedded;
            this.fieldName = fieldName;
        }
    }

}
