package org.observertc.webrtc.observer.configbuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.observertc.webrtc.observer.common.ObjectMapperInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigHolder<T> {
    public static final String DEFAULT_DELIMITER = ".";
    private static final Logger logger = LoggerFactory.getLogger(ConfigHolder.class);

    private ConfigNode configNode;
    private Map<String, Object> previous;
    private Map<String, Object> actual;
    private final ConfigConverter<T> configConverter;

    public ConfigHolder(T initial, Class<T> klass) {
        this(ConfigConverter.convertToMap(initial), klass, ConfigNode.make(klass));
    }

    public ConfigHolder(Map<String, Object> initial, Class<T> klass) {
        this(initial, klass, ConfigNode.make(klass));
    }

    public ConfigHolder(Map<String, Object> initial, Class<T> klass, ConfigNode configNode) {
        this.actual = initial;
        this.configConverter = new ConfigConverter<>(klass);
        this.configNode = configNode;
    }

    public void renew(Map<String, Object> updatedConfig) {
        Map<String, Object> newConfig;
        try {
            ObjectMapper mapper = ObjectMapperInstance.get();
            byte[] bytes = mapper.writeValueAsBytes(updatedConfig);
            newConfig = mapper.readValue(bytes, Map.class);
        } catch (IOException e) {
            logger.warn("Exception occurred while renewing configuration. THe configuration is not renewed", e);
            return;
        }
        this.previous = this.actual;
        this.actual = newConfig;
    }

    public Map<String, Object> getAdditions() {
        if (Objects.isNull(this.previous)) {
            return this.actual;
        }
        Map<String, ConfigNode> configNodes = getConfigNodeChildren(this.configNode);
        return this.getMapSurplus(this.previous, this.actual, configNodes);
    }

    public Map<String, Object> getAdditionsFlatMap() {
        return this.getAdditionsFlatMap(DEFAULT_DELIMITER);
    }

    public Map<String, Object> getAdditionsFlatMap(String delimiter) {
        Map<String, Object> additions = this.getAdditions();
        Map<String, ConfigNode> configNodes = getConfigNodeChildren(this.configNode);
        Map<String, Object> result = this.getFlatMap(additions, delimiter, configNodes);
        return result;
    }

    public Map<String, Object> getRemovals() {
        if (Objects.isNull(this.actual)) {
            return this.previous;
        }
        Map<String, ConfigNode> predicates = getConfigNodeChildren(this.configNode);
        return this.getMapSurplus(this.actual, this.previous, predicates);
    }

    public Map<String, Object> getRemovalsFlatMap() {
        return this.getRemovalsFlatMap(DEFAULT_DELIMITER);
    }

    public Map<String, Object> getRemovalsFlatMap(String delimiter) {
        Map<String, Object> removals = this.getRemovals();
        Map<String, ConfigNode> configNodes = getConfigNodeChildren(this.configNode);
        Map<String, Object> result = this.getFlatMap(removals, delimiter, configNodes);
        return result;
    }

    public T getConfig() {
        return this.configConverter.apply(this.actual);
    }

    public Map<String, Object> getConfigMap() {
        try {
            ObjectMapper mapper = ObjectMapperInstance.get();
            byte[] bytes = mapper.writeValueAsBytes(this.actual);
            return mapper.readValue(bytes, Map.class);
        } catch (IOException e) {
            logger.warn("Exception occurred while renewing configuration. THe configuration is not renewed", e);
            return Collections.EMPTY_MAP;
        }
    }

    private Map getMapSurplus(Map baseMap, Map subjectMap, Map<String, ConfigNode> predicates) {
        Map result = new HashMap();
        Iterator<Map.Entry> it = baseMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = it.next();
            Object key = entry.getKey();
            Object baseValue = entry.getValue();
            Object subjectValue = subjectMap.get(key);
            if (Objects.isNull(subjectValue)) {
                continue;
            }
            ConfigNode predicate = predicates == null ? null : predicates.get(key);
            Object diff = getValueSurplus(baseValue, subjectValue, predicate);
            if (Objects.nonNull(diff)) {
                result.put(key, diff);
            }
        }

        it = subjectMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = it.next();
            Object key = entry.getKey();
            Object subjectValue = entry.getValue();
            Object baseValue = baseMap.get(key);
            if (Objects.nonNull(baseValue)) {
                continue;
            }
            result.put(key, subjectValue);
        }
        return result;
    }

    private Map<String, Object> getFlatMap(Map<String, Object> baseMap, String delimiter, Map<String, ConfigNode> configNodes) {
        Iterator<Map.Entry<String, Object>> it = baseMap.entrySet().iterator();
        Map<String, Object> result = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            Object baseValue = entry.getValue();
            ConfigNode configNode = configNodes.get(key);
            if (Objects.isNull(baseValue)) {
                continue;
            }
            if (baseValue instanceof Map) {
                Map<String, ConfigNode> childConfigNodes = configNode == null ? Collections.EMPTY_MAP : configNode.children;
                getFlatMap((Map<String, Object>) baseValue, delimiter, childConfigNodes)
                        .entrySet()
                        .stream()
                        .forEach(childEntry -> {
                            String childKey = String.join(delimiter, key, childEntry.getKey());
                            result.put(childKey, childEntry.getValue());
                        });
            } else if (baseValue instanceof List) {
                Map<String, ConfigNode> childConfigNodes = configNode == null ? Collections.EMPTY_MAP : configNode.children;
                Function<Object, String> keyMaker = getKeyMaker(configNode);
                Map<String, Object> childMap = ((List<?>) baseValue).stream().collect(Collectors.toMap(keyMaker, Function.identity()));
                getFlatMap(childMap, delimiter, childConfigNodes)
                        .entrySet()
                        .stream()
                        .forEach(childEntry -> {
                            String childKey = String.join(delimiter, key, childEntry.getKey());
                            result.put(childKey, childEntry.getValue());
                        });
            } else {
                result.put(key, baseValue);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private Object getValueSurplus(Object baseValue, Object subjectValue, ConfigNode configNode) {
        if (subjectValue == null) return null;
        if (baseValue == null) return subjectValue;
        Map<String, ConfigNode> configNodes = getConfigNodeChildren(configNode);
        if (baseValue instanceof Map && subjectValue instanceof Map) return getMapSurplus((Map) baseValue, (Map) subjectValue, configNodes);
        if (baseValue instanceof List && subjectValue instanceof List) {
            Function<Object, String> keyMaker = getKeyMaker(configNode);
            Map baseMap = ((List<?>) baseValue).stream().collect(Collectors.toMap(keyMaker, Function.identity()));
            Map subjectMap = ((List<?>) subjectValue).stream().collect(Collectors.toMap(keyMaker, Function.identity()));
            Map diffMap = getMapSurplus(baseMap, subjectMap, configNodes);
            if (Objects.isNull(diffMap) || diffMap.size() < 1) {
                return null;
            }
            return diffMap.values().stream().collect(Collectors.toList());
        }
        return baseValue.equals(subjectValue) ? null : subjectValue;
    }

    private static Map<String, ConfigNode> getConfigNodeChildren(ConfigNode configNode) {
        if (Objects.isNull(configNode)) {
            return Collections.EMPTY_MAP;
        }
        if (Objects.isNull(configNode.children)) {
            return Collections.EMPTY_MAP;
        }
        return configNode.children;
    }

    private static Function<Object, String> getKeyMaker(ConfigNode configNode) {
        Function<Object, String> result;
        if (Objects.isNull(configNode) || Objects.isNull(configNode.keyMaker)) {
            AtomicInteger indexGenerator = new AtomicInteger();
            result = o -> String.valueOf(indexGenerator.incrementAndGet());
        } else {
            result = configNode.keyMaker;
        }
        return result;
    }
}
