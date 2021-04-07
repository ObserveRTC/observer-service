package org.observertc.webrtc.observer.connectors.encoders;

import org.apache.avro.Schema;
import org.observertc.webrtc.observer.common.TaskAbstract;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class SchemaMapperAbstract<T> extends TaskAbstract<Map<ReportType, ReportMapperAbstract<T>>> {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(SchemaMapperAbstract.class);

    private final Map<ReportType, Schema> schemaMap = new HashMap<>();
    protected Logger logger = DEFAULT_LOGGER;

    public SchemaMapperAbstract() {
        this.schemaMap.put(ReportType.INITIATED_CALL, InitiatedCall.getClassSchema());
        this.schemaMap.put(ReportType.FINISHED_CALL, FinishedCall.getClassSchema());
        this.schemaMap.put(ReportType.JOINED_PEER_CONNECTION, JoinedPeerConnection.getClassSchema());
        this.schemaMap.put(ReportType.DETACHED_PEER_CONNECTION, DetachedPeerConnection.getClassSchema());
        this.schemaMap.put(ReportType.INBOUND_RTP, InboundRTP.getClassSchema());
        this.schemaMap.put(ReportType.REMOTE_INBOUND_RTP, RemoteInboundRTP.getClassSchema());
        this.schemaMap.put(ReportType.OUTBOUND_RTP, OutboundRTP.getClassSchema());
        this.schemaMap.put(ReportType.ICE_CANDIDATE_PAIR, ICECandidatePair.getClassSchema());
        this.schemaMap.put(ReportType.ICE_LOCAL_CANDIDATE, ICELocalCandidate.getClassSchema());
        this.schemaMap.put(ReportType.ICE_REMOTE_CANDIDATE, ICERemoteCandidate.getClassSchema());
        this.schemaMap.put(ReportType.TRACK, Track.getClassSchema());
        this.schemaMap.put(ReportType.MEDIA_SOURCE, MediaSource.getClassSchema());
        this.schemaMap.put(ReportType.USER_MEDIA_ERROR, UserMediaError.getClassSchema());
        this.schemaMap.put(ReportType.MEDIA_DEVICE, MediaDevice.getClassSchema());
        this.schemaMap.put(ReportType.CLIENT_DETAILS, ClientDetails.getClassSchema());
        this.schemaMap.put(ReportType.EXTENSION, ExtensionReport.getClassSchema());
        this.schemaMap.put(ReportType.OBSERVER_EVENT, ObserverEventReport.getClassSchema());
    }

    @Override
    protected Map<ReportType, ReportMapperAbstract<T>> perform() throws Throwable {
        Iterator<Map.Entry<ReportType, Schema>> it = this.schemaMap.entrySet().iterator();
        Map<ReportType, ReportMapperAbstract<T>> result = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry<ReportType, Schema> entry = it.next();
            ReportType reportType = entry.getKey();
            Schema schema = entry.getValue();
            ReportMapperAbstract<T> reportMapper = this.makeReportMapper(reportType, schema);
            result.put(reportType, reportMapper);
        }
        return result;
    }

    protected ReportMapperAbstract<T> makeReportMapper(ReportType reportType, Schema schema) {
        ReportMapperAbstract<T> result = new ReportMapperAbstract<T>();
        Supplier<T> supplier = this.makeSupplier(reportType);
        result.withSupplier(supplier);
        // common fields
        List<String> excludedFields = List.of("payload", "version");
        Report.getClassSchema().getFields().stream().forEach(field -> {
            if (excludedFields.stream().anyMatch(excludedField -> field.name().equals(excludedField))) {
                return;
            }
            var fieldName = field.name();
            var fieldSchema = field.schema();
            var fieldInfo = makeFieldInfo(fieldName, fieldSchema.getType(), fieldSchema);
            BiConsumer<T, Object> mapper = this.makeFieldMapper(fieldInfo);
            result.add(fieldName, mapper);
        });
        ReportMapperAbstract<T> payloadMapper = new ReportMapperAbstract<T>();
        this.schemaFieldWalker(schema, fieldInfo -> {
            BiConsumer<T, Object> mapper = this.makeFieldMapper(fieldInfo);
            payloadMapper.add(fieldInfo.fieldName, mapper);
        }, List.of("payload"));
        result.add("payload", payloadMapper);
        return result;
    }
    protected abstract Supplier<T> makeSupplier(ReportType reportType);

    protected abstract BiConsumer<T, Object> makeFieldMapper(FieldInfo fieldInfo);

    public SchemaMapperAbstract withLogger(Logger logger) {
        this.logger.info("Default logger for {} is switched to {}", this.getClass().getSimpleName(), logger.getName());
        this.logger = logger;
        return this;
    }

    protected void schemaFieldWalker(Schema schema, Consumer<FieldInfo> evaluator, List<String> excluding) {
        for (Schema.Field field : schema.getFields()) {
            String fieldName = field.name();
            if (Objects.nonNull(excluding) && excluding.stream().anyMatch(excluded -> excluded.equals(fieldName))) {
                continue;
            }
            Schema fieldSchema = field.schema();
            Schema.Type fieldType = fieldSchema.getType();
            FieldInfo fieldInfo = this.makeFieldInfo(fieldName, fieldType, fieldSchema);
            evaluator.accept(fieldInfo);
        }
    }

    private FieldInfo makeFieldInfo(String fieldName, Schema.Type fieldType, Schema fieldSchema) {
        if (fieldType.equals(Schema.Type.UNION) && fieldSchema.getTypes().size() == 2) {
            // these are most likely types of the avro schema where null union with the actual type
            Schema subSchema = fieldSchema.getTypes().get(0);
            if (subSchema.getType().equals(Schema.Type.NULL)) { // most likely nullable
                subSchema = fieldSchema.getTypes().get(1);
            }
            FieldInfo fieldInfo = new FieldInfo(subSchema, true, fieldName);
            return fieldInfo;
        } else {
            FieldInfo fieldInfo = new FieldInfo(fieldSchema, false, fieldName);
            return fieldInfo;
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
