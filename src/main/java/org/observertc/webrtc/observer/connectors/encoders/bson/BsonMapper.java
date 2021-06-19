package org.observertc.webrtc.observer.connectors.encoders.bson;

import org.bson.Document;
import org.observertc.webrtc.observer.connectors.encoders.SchemaMapperAbstract;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class BsonMapper extends SchemaMapperAbstract<Document> {

    @Override
    protected Supplier<Document> makeSupplier(ReportType reportType) {
        return () -> new Document();
    }

    @Override
    protected BiConsumer<Document, Object> makeFieldMapper(FieldInfo fieldInfo) {
        switch (fieldInfo.schema.getType()) {
            case DOUBLE:
            case FLOAT:
            case STRING:
            case BYTES:
            case LONG:
            case INT:
            case BOOLEAN:
                return (document, value) -> {
                    document.append(fieldInfo.fieldName, value);
                };
            default:
            case ENUM:
                return (document, value) -> {
                    document.append(fieldInfo.fieldName, value.toString());
                };
            case UNION:
            case RECORD:
            case ARRAY:
            case MAP:
            case NULL:
                throw new RuntimeException("Cannot map type " + fieldInfo.schema.getType());


        }
    }
}
