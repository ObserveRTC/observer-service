package org.observertc.observer.configbuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.observertc.observer.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigOperations {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(ConfigOperations.class);

    private final boolean mustMatchClasses;
    private final Map<String, Object> subject;
    private ConfigNode configNode;
    private List<String> errors = new LinkedList<>();

    public ConfigOperations(Map<String, Object> subject) throws IOException {
        this(subject, false);
    }

    public ConfigOperations(Map<String, Object> subject, boolean mustMatchClasses) throws IOException {
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(subject);
        this.subject = OBJECT_MAPPER.readValue(bytes, Map.class);
        this.mustMatchClasses = mustMatchClasses;
    }

    public ConfigOperations withConfigNode(ConfigNode configNode) {
        this.configNode = configNode;
        return this;
    }

    public ConfigOperations replace(Map<String, Object> map) {
        this.errors.clear();
        this.subject.clear();
        this.subject.putAll(map);
        return this;
    }

    public List<String> getErrors() {
        var result = this.errors;
        this.errors = new LinkedList<>();
        return result;
    }

    public ConfigOperations add(Map<String, Object> map) {
        Map<String, ConfigNode> predicates = this.configNode != null ? this.configNode.children : Collections.EMPTY_MAP;
        this.reduceMaps(this.subject, map, predicates);
        return this;
    }

    public Map<String, Object> getPath(List<String> keys) {
        Map<String, ConfigNode> configNodes = this.configNode != null ? this.configNode.children : Collections.EMPTY_MAP;
        Map<String, Object> result = this.subject;
        for (int i = 0; i < keys.size(); ++i) {
            String key = keys.get(i);
            Object value = result.get(key);
            boolean lastKey = i == keys.size() - 1;
            ConfigNode configNode = configNodes == null ? null : configNodes.get(key);
            if (Objects.isNull(value)) {
                return Collections.EMPTY_MAP;
            }
            if (value instanceof Map) {
                result = (Map<String, Object>) value;
                configNodes = configNode == null ? null : configNode.children;
                continue;
            } else if (value instanceof List) {
                if (lastKey) { // last key
                    return Map.of(key, value);
                }
                Function<Object, String> keyMaker;
                if (configNode == null || configNode.keyMaker == null) {
                    keyMaker = Object::toString;
                } else {
                    keyMaker = configNode.keyMaker;
                }
                Map newValues = ((List<?>) value).stream().collect(Collectors.toMap(keyMaker, Function.identity()));
                result = newValues;
            }
        }
        return result;
    }

    public ConfigOperations remove(List<String> keys) {
        Map<String, ConfigNode> configNodes = this.configNode != null ? this.configNode.children : Collections.EMPTY_MAP;
        Map<String, Object> values = this.subject;
        for (int i = 0; i < keys.size(); ++i) {
            boolean lastKey = i == keys.size() - 1;
            String key = keys.get(i);
            Object value = values.get(key);
            ConfigNode configNode = configNodes == null ? null : configNodes.get(key);
            if (Objects.isNull(value)) {
                return this;
            }
            if (Objects.nonNull(configNode) && !configNode.mutable) {
                throw new IllegalStateException("field " + key + " is immutable");
            }
            if (lastKey) {
                values.remove(key);
                return this;
            }
            if (value instanceof Map) {
                values = (Map<String, Object>) value;
                configNodes = configNode == null ? null : configNode.children;
                continue;
            } else if (value instanceof List) {
                Function<Object, String> keyMaker;
                if (configNode == null || configNode.keyMaker == null) {
                    keyMaker = Object::toString;
                } else {
                    keyMaker = configNode.keyMaker;
                }
                Map newValues = ((List<?>) value).stream().collect(Collectors.toMap(keyMaker, Function.identity()));
                if (i + 1 < keys.size() - 1) { // the next key is not the last key
                    values = newValues;
                    continue;
                }
                String nextKey = keys.get(++i);
                newValues.remove(nextKey);
                values.put(key, newValues.values().stream().collect(Collectors.toList()));
                return this;
            }
        }
        return this;
    }

    public Map<String, Object> makeConfig() throws IOException {
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(this.subject);
        Map result = OBJECT_MAPPER.readValue(bytes, Map.class);
        return result;
    }

    private Map reduceMaps(Map result, Map newMap, Map<String, ConfigNode> configNodes) {
        if (Utils.allNull(result, newMap)) {
            return null;
        }
        if (Objects.isNull(result)) {
            return newMap;
        }
        if (Objects.isNull(newMap)) {
            return result;
        }
        Iterator<Map.Entry<String, Object>> it = newMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            Object mapValue = entry.getValue();
            Object currentValue = result.get(key);
            ConfigNode configNode = configNodes == null ? null : configNodes.get(key);
            Object value = this.reduceValues(currentValue, mapValue, configNode);
            result.put(key, value);
        }
        return result;
    }

    private List reduceLists(List result, List newList, ConfigNode configNode) {
        if (Utils.allNull(result, newList)) {
            return null;
        }
        if (Objects.isNull(result)) {
            return newList;
        }
        if (Objects.isNull(newList)) {
            return result;
        }
        AtomicInteger index = new AtomicInteger();
        Function<Object, String> keyMaker;
        if (configNode != null && configNode.keyMaker != null) {
            keyMaker = configNode.keyMaker;
        } else {
            keyMaker = o -> Integer.toString(index.incrementAndGet());
        }
        Map<String, Object> resultMap = (Map<String, Object>) result.stream().collect(Collectors.toMap(keyMaker, o -> o));
        Map<String, Object> newMap = (Map<String, Object>) newList.stream().collect(Collectors.toMap(keyMaker, o -> o));
        resultMap = this.reduceMaps(resultMap, newMap, configNode == null ? null : configNode.children);
        return resultMap.values().stream().collect(Collectors.toList());
    }

    private Object reduceValues(Object actualValue, Object newValue, ConfigNode configNode) {
        if (Utils.allNull(actualValue, newValue)) {
            return null;
        }
        boolean immutable = Objects.nonNull(configNode) && !configNode.mutable;
        if (Objects.isNull(actualValue)) {
            if (immutable) {
                this.errors.add("Attempted to alter value null value, which belongs to an immutable key");
                return actualValue;
            }
            return newValue;
        }
        if (Objects.isNull(newValue)) {
            if (immutable) {
                this.errors.add("Attempted to alter value " + actualValue.toString() + " to null, but the key the value belongs to is immutable");
                return actualValue;
            }
            return actualValue;
        }
        if (actualValue instanceof Map && newValue instanceof Map) {
            Map<String, ConfigNode> configNodes = configNode == null ? Collections.EMPTY_MAP : configNode.children;
            return this.reduceMaps((Map) actualValue, (Map) newValue, configNodes);
        }
        if (actualValue instanceof List && newValue instanceof List) {
            return this.reduceLists((List) actualValue, (List) newValue, configNode);
        }
        if (!actualValue.getClass().equals(newValue.getClass())) {
            if (this.mustMatchClasses) {
                logger.info("Values mismtach classes: {}, {}",
                        actualValue.getClass().getName(),
                        newValue.getClass().getName()
                );
                return actualValue;
            } else {
                return newValue;
            }
        }
        if (immutable && !actualValue.equals(newValue)) {
            this.errors.add("Attempted to alter value " + actualValue.toString() + " but it is immutable");
            return actualValue;
        }
        return newValue;
    }
}
