package org.observertc.webrtc.observer.codecs;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.*;
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
import java.util.function.Function;
import java.util.function.Supplier;

public class JsonToAvroBuilder<T> extends StdDeserializer<T> {

    private static final Logger logger = LoggerFactory.getLogger(JsonToAvroBuilder.class);

    private Map<String, BiConsumer<T, JsonNode>> recordSetters = new HashMap<>();
    private Supplier<T> instanceProvider;

    JsonToAvroBuilder(Class<T> klass, Schema schema, Supplier<T> instanceProvider) throws NoSuchMethodException {
        super(klass);
        this.instanceProvider = instanceProvider;
        for (var field : schema.getFields()) {
            var fieldName = field.name();
            var methodNameStub = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            var setMethodName = "set" + methodNameStub;
            Schema.Type type = AvroBuilderToJson.getType(field.schema());
            Function<JsonNode, Object> jnodeGetter = makeJsonNodeGetter(fieldName, type);
            Method setMethod = klass.getDeclaredMethod(setMethodName);
            this.recordSetters.put(fieldName, (record, node) -> {
                Object value = jnodeGetter.apply(node);
                if (Objects.isNull(value)) {
                    return;
                }
                try {
                    setMethod.invoke(record, value);
                } catch (IllegalAccessException e) {
                    logger.warn("illegal recordSetters access to {}", record.getClass().getSimpleName(), e);
                    return;
                } catch (InvocationTargetException e) {
                    logger.warn("invalid recordSetters invocation access to {}", record.getClass().getSimpleName(), e);
                    return;
                }
            });
        }
    }

    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        T result = this.instanceProvider.get();
        JsonNode node = jp.getCodec().readTree(jp);
        this.recordSetters.forEach((fieldName, methodInvoker) -> {
            methodInvoker.accept(result, node);
        });
        int id = (Integer) ((IntNode) node.get("id")).numberValue();
        String itemName = node.get("itemName").asText();
        int userId = (Integer) ((IntNode) node.get("createdBy")).numberValue();
        return null;
    }

    private static Function<JsonNode, Object> makeJsonNodeGetter(String fieldName, Schema.Type type) {
        switch (type) {
            case BOOLEAN:
                return (node) -> {
                    return (Boolean) ((BooleanNode) node.get(fieldName)).booleanValue();
                };
            case INT:
                return (node) -> {
                    return (Integer) ((IntNode) node.get(fieldName)).numberValue();
                };
            case LONG:
                return (node) -> {
                    return (Long) ((LongNode) node.get(fieldName)).numberValue();
                };
            case FLOAT:
                return (node) -> {
                    return (Float) ((FloatNode) node.get(fieldName)).numberValue();
                };
            case DOUBLE:
                return (node) -> {
                    return (Double) ((DoubleNode) node.get(fieldName)).numberValue();
                };
            case BYTES:
                return (node) -> {
                    return (byte[]) ((BinaryNode) node.get(fieldName)).binaryValue();
                };
            case STRING:
                return (node) -> {
                    return (String) ((TextNode) node.get(fieldName)).asText();
                };
            default:
                throw new IllegalStateException("json converting has not been implemented for type " + type.getName());
        }
    }
}
