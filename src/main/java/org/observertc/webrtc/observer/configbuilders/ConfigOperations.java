package org.observertc.webrtc.observer.configbuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.observertc.webrtc.observer.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

public class ConfigOperations {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(ConfigOperations.class);

    private final boolean mustMatchClasses;
    private final Map<String, Object> subject;
    private Map<String, NodePredicate> predicates = new HashMap<>();

    public ConfigOperations(Map<String, Object> subject) throws IOException {
        this(subject, false);
    }

    public ConfigOperations(Map<String, Object> subject, boolean mustMatchClasses) throws IOException {
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(subject);
        this.subject = OBJECT_MAPPER.readValue(bytes, Map.class);
        this.mustMatchClasses = mustMatchClasses;
    }

    public ConfigOperations add(Map<String, Object> map) {
        this.reduceMaps(this.subject, map, this.predicates);
        return this;
    }

    public ConfigOperations withIndexOfPredicate(List<String> keys, BiFunction<List, Object, Integer> indexOf) {
        if (Objects.isNull(keys) && keys.size() < 1) {
            throw new IllegalStateException("keys for predicate must be provided");
        }
        Map<String, NodePredicate> predicates = this.predicates;
        for (int i = 0; i < keys.size(); ++i) {
            String key = keys.get(i);
            boolean lastKey = i == keys.size() - 1;
            NodePredicate nodePredicate = predicates.get(key);
            if (Objects.nonNull(nodePredicate)) {
                if (lastKey) {
                    throw new IllegalStateException("Cannot have two predicate for the same key: " + key);
                }
                predicates = nodePredicate.children;
                continue;
            }
            nodePredicate = new NodePredicate();
            predicates.put(key, nodePredicate);
            if (lastKey) {
                nodePredicate.indexOf = indexOf;
            }
        }
        return this;
    }

    public Map<String, Object> makeConfig() throws IOException {
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(this.subject);
        Map result = OBJECT_MAPPER.readValue(bytes, Map.class);
        return result;
    }

    private Map reduceMaps(Map result, Map newMap, Map<String, NodePredicate> predicates) {
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
            NodePredicate predicate = predicates == null ? null : predicates.get(key);
            Object value = this.reduceValues(currentValue, mapValue, predicate);
            result.put(key, value);
        }
        return result;
    }

    private List reduceLists(List result, List newList, NodePredicate predicate) {
        if (Utils.allNull(result, newList)) {
            return null;
        }
        if (Objects.isNull(result)) {
            return newList;
        }
        if (Objects.isNull(newList)) {
            return result;
        }
        BiFunction<List, Object, Integer> indexOf = null;
        if (Objects.isNull(predicate) || Objects.isNull(predicate.indexOf)) {
            indexOf = (list, item) -> list.indexOf(item);
        } else {
            indexOf = predicate.indexOf;
        }
        for (Object newItem : newList) {
            int index = indexOf.apply(result, newItem);
            if (index < 0) {
                // not found
                result.add(newItem);
                continue;
            }
            Object currentItem = result.get(index);
            Object value = this.reduceValues(currentItem, newItem, predicate);
            result.set(index, value);
        }
        return result;
    }

    private Object reduceValues(Object actualValue, Object newValue, NodePredicate predicate) {
        if (Utils.allNull(actualValue, newValue)) {
            return null;
        }
        if (Objects.isNull(actualValue)) {
            return newValue;
        }
        if (Objects.isNull(newValue)) {
            return actualValue;
        }
        if (actualValue instanceof Map && newValue instanceof Map) {
            Map<String, NodePredicate> predicates = predicate == null ? null : predicate.children;
            return this.reduceMaps((Map) actualValue, (Map) newValue, predicates);
        }
        if (actualValue instanceof List && newValue instanceof List) {
            return this.reduceLists((List) actualValue, (List) newValue, predicate);
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
        return newValue;
    }

    private class NodePredicate {
        BiFunction<List, Object, Integer> indexOf;
        final Map<String, NodePredicate> children = new HashMap<>();
        private NodePredicate() {
        }
    }


}
