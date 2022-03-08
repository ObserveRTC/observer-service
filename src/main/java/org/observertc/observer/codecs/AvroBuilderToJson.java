package org.observertc.observer.codecs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class AvroBuilderToJson<T> extends StdSerializer<T> {

    private static final Logger logger = LoggerFactory.getLogger(AvroBuilderToJson.class);

    static Schema.Type getType(Schema fieldSchema) {
        Schema.Type fieldType = fieldSchema.getType();
        if (fieldType.equals(Schema.Type.UNION) && fieldSchema.getTypes().size() == 2) {
            // these are most likely types of the avro schema where null union with the actual type
            Schema subSchema = fieldSchema.getTypes().get(0);
            if (subSchema.getType().equals(Schema.Type.NULL)) { // most likely nullable
                subSchema = fieldSchema.getTypes().get(1);
            }
            return subSchema.getType();
        } else {
            return fieldType;
        }
    }

    private Map<String, BiConsumer<T, JsonGenerator>> jsonSetters = new HashMap<>();
    private Map<String, BiConsumer<T, JsonNode>> recordSetters = new HashMap<>();

    AvroBuilderToJson(Class<T> klass, Schema schema) throws NoSuchMethodException {
        super(klass);
        for (var field : schema.getFields()) {
            var fieldName = field.name();
            var methodNameStub = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            var getMethodName = "get" + methodNameStub;
            Schema.Type type = getType(field.schema());
            BiConsumer<JsonGenerator, Object> jsonSetter = makeJGenSetter(fieldName, type);
            Method getMethod = klass.getDeclaredMethod(getMethodName);
            this.jsonSetters.put(fieldName, (record, jgen) -> {
                try {
                    var result = getMethod.invoke(record);
                    if (Objects.isNull(result)) {
                        return;
                    }
                    jsonSetter.accept(jgen, result);
                } catch (IllegalAccessException e) {
                    logger.warn("illegal jsonSetters access to {}", record.getClass().getSimpleName(), e);
                    return;
                } catch (InvocationTargetException e) {
                    logger.warn("invalid jsonSetters invocation access to {}", record.getClass().getSimpleName(), e);
                    return;
                }
            });
        }
    }

    @Override
    public void serialize(T value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        this.jsonSetters.forEach((fieldName, methodInvoker) -> {
            methodInvoker.accept(value, jgen);
        });
        jgen.writeEndObject();
    }

    private static BiConsumer<JsonGenerator, Object> makeJGenSetter(String fieldName, Schema.Type type) {
        switch (type) {
            case BOOLEAN:
                return (jgen, obj) -> {
                    try {
                        jgen.writeBooleanField(fieldName, (Boolean) obj);
                    } catch(Exception ex) {
                        logger.warn("Exception while writing jgen type {}, obj {}", type, obj, ex);
                    }
                };
            case INT:
                return (jgen, obj) -> {
                    try {
                        jgen.writeNumberField(fieldName, (Integer) obj);
                    } catch(Exception ex) {
                        logger.warn("Exception while writing jgen type {}, obj {}", type, obj, ex);
                    }
                };
            case LONG:
                return (jgen, obj) -> {
                    try {
                        jgen.writeNumberField(fieldName, (Long) obj);
                    } catch(Exception ex) {
                        logger.warn("Exception while writing jgen type {}, obj {}", type, obj, ex);
                    }
                };
            case FLOAT:
                return (jgen, obj) -> {
                    try {
                        jgen.writeNumberField(fieldName, (Float) obj);
                    } catch(Exception ex) {
                        logger.warn("Exception while writing jgen type {}, obj {}", type, obj, ex);
                    }
                };
            case DOUBLE:
                return (jgen, obj) -> {
                    try {
                        jgen.writeNumberField(fieldName, (Double) obj);
                    } catch(Exception ex) {
                        logger.warn("Exception while writing jgen type {}, obj {}", type, obj, ex);
                    }
                };
            case BYTES:
                return (jgen, obj) -> {
                    try {
                        jgen.writeBinaryField(fieldName, (byte[]) obj);
                    } catch(Exception ex) {
                        logger.warn("Exception while writing jgen type {}, obj {}", type, obj, ex);
                    }
                };
            case STRING:
                return (jgen, obj) -> {
                    try {
                        jgen.writeStringField(fieldName, (String) obj);
                    } catch(Exception ex) {
                        logger.warn("Exception while writing jgen type {}, obj {}", type, obj, ex);
                    }
                };
            default:
                throw new IllegalStateException("json converting has not been implemented for type " + type.getName());
        }
    }
}
