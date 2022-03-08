package org.observertc.observer.codecs;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.*;
import org.apache.avro.Schema;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
            var paramClasses = getTypeJavaClasses(type);
            Function<JsonNode, Object> jnodeGetter = makeJsonNodeGetter(fieldName, type);
//            Arrays.stream(klass.getMethods()).forEach(method -> logger.info("{}: {}", method.getName(), method.getParameterTypes()));
            Method foundMethod = null;
            Class foundParamClass = null;
            for (var paramClass : paramClasses) {
                try {
                    foundMethod = klass.getDeclaredMethod(setMethodName, paramClass);
                    foundParamClass = paramClass;
                    break;
                } catch (NoSuchMethodException ex) {
                    foundMethod = null;
                }
            }
            if (Objects.isNull(foundMethod)) {
                String params = String.join(", ", paramClasses.stream().map(k -> k.getName()).collect(Collectors.toList()));
                throw new NoSuchMethodException("No method " + setMethodName + " has found in " + klass.getName() + " with parameters " + params);
            }
            final Method setMethod = foundMethod;
            final Class paramClass = foundParamClass;
            this.recordSetters.put(fieldName, (record, node) -> {
                Object paramValue = jnodeGetter.apply(node);
                if (Objects.isNull(paramValue)) {
                    return;
                }
                Class paramValueClass = paramValue.getClass();
                if (!paramValueClass.equals(paramClass)) {
                    if (paramClass.equals(Long.class)) {
                        if (paramValueClass.equals(Integer.class)) {
                            long l = ((Integer) paramValue).longValue();
                            paramValue = Long.valueOf(l);
                        } else if (paramValueClass.equals(int.class)) {
                            int i = (int) paramValue;
                            paramValue = Long.valueOf(i);
                        }
                    } else if (paramClass.equals(long.class)) {
                        if (paramValueClass.equals(Integer.class)) {
                            long l = ((Integer) paramValue).longValue();
                            paramValue = (long) Long.valueOf(l);
                        } else if (paramValueClass.equals(int.class)) {
                            int i = (int) paramValue;
                            paramValue = (long) i;
                        }
                    }
                } else {
                    paramValue = paramClass.cast(paramValue);
                }
                try {
                    try {
                        setMethod.invoke(record, paramValue);
                    } catch (ClassCastException e) {
                    }

                } catch (IllegalAccessException e) {
                    logger.warn("illegal recordSetters access to {}", record.getClass().getSimpleName(), e);
                    return;
                } catch (InvocationTargetException e) {
                    logger.warn("invalid recordSetters invocation access to {}", record.getClass().getSimpleName(), e);
                    return;
                } catch (IllegalArgumentException e) {
                    logger.warn("invalid recordSetters argument access to {}", record.getClass().getSimpleName(), e);
                    return;
                } catch (ClassCastException e) {
                    logger.warn("invalid recordSetters argument access to {}", record.getClass().getSimpleName(), e);
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
        return result;
    }

    private static Function<JsonNode, Object> makeJsonNodeGetter(String fieldName, Schema.Type type) {
        switch (type) {
            case BOOLEAN:
                return (node) -> {
                    if (!node.has(fieldName)) return null;
                    return (Boolean) ((BooleanNode) node.get(fieldName)).booleanValue();
                };
            case INT:
                return (node) -> {
                    if (!node.has(fieldName)) return null;
                    return (Integer) ((IntNode) node.get(fieldName)).numberValue();
                };
            case LONG:
                return (node) -> {
                    if (!node.has(fieldName)) return null;
                    var actualNode = node.get(fieldName);
                    if (actualNode.canConvertToInt()) {
                        return (Integer) ((IntNode) actualNode).numberValue();
                    } else if (actualNode.canConvertToLong()) {
                        return (Long) ((LongNode) actualNode).numberValue();
                    } else {
                        logger.warn("{} cannot convert to anything?", actualNode);
                        return null;
                    }
                };
            case FLOAT:
                return (node) -> {
                    if (!node.has(fieldName)) return null;
                    return (Float) ((FloatNode) node.get(fieldName)).numberValue();
                };
            case DOUBLE:
                return (node) -> {
                    if (!node.has(fieldName)) return null;
                    return (Double) ((DoubleNode) node.get(fieldName)).numberValue();
                };
            case BYTES:
                return (node) -> {
                    if (!node.has(fieldName)) return null;
                    return (byte[]) ((BinaryNode) node.get(fieldName)).binaryValue();
                };
            case STRING:
                return (node) -> {
                    if (!node.has(fieldName)) return null;
                    return (String) ((TextNode) node.get(fieldName)).asText();
                };
            default:
                throw new IllegalStateException("json converting has not been implemented for type " + type.getName());
        }
    }

    private static List<Class> getTypeJavaClasses(Schema.Type type) {
        switch (type) {
            case BOOLEAN:
                return List.of(Boolean.class, boolean.class);
            case INT:
                return List.of(Integer.class, int.class);
            case LONG:
                return List.of(Long.class, long.class);
            case FLOAT:
                return List.of(Float.class, float.class);
            case DOUBLE:
                return List.of(Double.class, double.class);
            case BYTES:
                return List.of(Bytes.class, byte[].class);
            case STRING:
                return List.of(String.class);
            default:
                throw new IllegalStateException("Cannot find appropriate class for " + type.getName());
        }
    }
}
