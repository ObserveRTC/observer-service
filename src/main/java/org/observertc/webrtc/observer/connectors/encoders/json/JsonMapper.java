package org.observertc.webrtc.observer.connectors.encoders.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.observertc.webrtc.observer.connectors.encoders.SchemaMapperAbstract;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class JsonMapper extends SchemaMapperAbstract<ObjectNode> {
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected Supplier<ObjectNode> makeSupplier(ReportType reportType) {
        return OBJECT_MAPPER::createObjectNode;
    }

    @Override
    protected BiConsumer<ObjectNode, Object> makeFieldMapper(FieldInfo fieldInfo) {
        switch (fieldInfo.schema.getType()) {
            case DOUBLE:
                return (objectNode, value) ->  {
                    if (Objects.isNull(value)) {
                        objectNode.putNull(fieldInfo.fieldName);
                        return;
                    }
                    objectNode.put(fieldInfo.fieldName, (Double) value);
                };
            case FLOAT:
                return (objectNode, value) ->  {
                    if (Objects.isNull(value)) {
                        objectNode.putNull(fieldInfo.fieldName);
                        return;
                    }
                    objectNode.put(fieldInfo.fieldName, (Float) value);
                };
            case STRING:
                return (objectNode, value) ->  {
                    if (Objects.isNull(value)) {
                        objectNode.putNull(fieldInfo.fieldName);
                        return;
                    }
                    objectNode.put(fieldInfo.fieldName, (String) value);
                };
            case BYTES:
                return (objectNode, value) ->  {
                    if (Objects.isNull(value)) {
                        objectNode.putNull(fieldInfo.fieldName);
                        return;
                    }
                    String bytesValue = new String((byte[]) value);
                    objectNode.put(fieldInfo.fieldName, bytesValue);
                };
            case LONG:
                return (objectNode, value) ->  {
                    if (Objects.isNull(value)) {
                        objectNode.putNull(fieldInfo.fieldName);
                        return;
                    }
                    objectNode.put(fieldInfo.fieldName, (Long) value);
                };
            case INT:
                return (objectNode, value) ->  {
                    if (Objects.isNull(value)) {
                        objectNode.putNull(fieldInfo.fieldName);
                        return;
                    }
                    objectNode.put(fieldInfo.fieldName, (Integer) value);
                };
            case BOOLEAN:
                return (objectNode, value) ->  {
                    if (Objects.isNull(value)) {
                        objectNode.putNull(fieldInfo.fieldName);
                        return;
                    }
                    objectNode.put(fieldInfo.fieldName, (Boolean) value);
                };
            case ENUM:
                return (objectNode, value) ->  {
                    if (Objects.isNull(value)) {
                        objectNode.putNull(fieldInfo.fieldName);
                        return;
                    }
                    String enumValue = value.toString();
                    objectNode.put(fieldInfo.fieldName, enumValue);
                };

            default:
            case FIXED:
            case UNION:
            case RECORD:
            case ARRAY:
            case MAP:
            case NULL:
                throw new RuntimeException("Cannot map type " + fieldInfo.schema.getType());
        }
    }
}
