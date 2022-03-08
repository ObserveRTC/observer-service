package org.observertc.observer.configbuilders;

import io.micronaut.core.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ConfigNode {

    public static ConfigNode make(Class klass) {
        return make(klass, klass.getAnnotation(ConfigAssent.class));
    }

    public static ConfigNode make(Class klass, Annotation assent) {
        final List<Class> PRIMITIVE_TYPES = List.of(Long.class, Integer.class, Short.class, String.class, Character.class, Enum.class, Byte.class, Double.class, Float.class);
        ConfigNode result = new ConfigNode();
        if (Objects.nonNull(assent) && assent instanceof ConfigAssent) {
            result.mutable = ((ConfigAssent) assent).mutable();
            if (!StringUtils.isEmpty(((ConfigAssent) assent).keyField())) {
                if (klass.isAssignableFrom(List.class)) {
                    result.keyMaker = obj -> {
                      Map map = (Map) obj;
                      String key = ((ConfigAssent) assent).keyField();
                      var value = map.get(key);
                      if (Objects.isNull(value)) {
                          return null;
                      }
                      return value.toString();
                    };
//                    result.keyMaker = o -> (String) ((Map) o).get(((ConfigAssent) assent).keyField());
                } else {
                    throw new IllegalStateException("Only List type of klass can have keyField. The provided type is " + klass.getName());
                }
            }
        }
        boolean isPrimitive = PRIMITIVE_TYPES.stream().anyMatch(t -> klass.isAssignableFrom(t));
        if (isPrimitive) {
            return result;
        }
        for (Field field : klass.getFields()) {
            Annotation klassAnnotation = field.getType().getAnnotation(ConfigAssent.class);
            Annotation fieldAnnotation = field.getAnnotation(ConfigAssent.class);
            Annotation annotation = Objects.nonNull(fieldAnnotation) ? fieldAnnotation : klassAnnotation;
            if (Objects.isNull(annotation)) {
                // let's check if any ancestor has a ConfigAssent
                for (Class ancestor = field.getType().getSuperclass();
                     Objects.nonNull(ancestor) && Objects.isNull(annotation);
                     ancestor = ancestor.getSuperclass()) {
                    annotation = ancestor.getAnnotation(ConfigAssent.class);
                }
            }
            String name = field.getName();
            result.children.put(name, make(field.getType(), annotation));
        }
        return result;
    }

    boolean mutable = true;
    Function<Object, String> keyMaker;
    final Map<String, ConfigNode> children = new HashMap<>();

    ConfigNode() {

    }

    protected ConfigNode withKeyMaker(Function<Object, String> keyMaker) {
        this.keyMaker = keyMaker;
        return this;
    }

    public ConfigNode withConfigNode(List<String> keys, ConfigNode predicate) {
        if (Objects.isNull(keys) && keys.size() < 1) {
            throw new IllegalStateException("keys for predicate must be provided");
        }
        Map<String, ConfigNode> predicates = this.children;
        for (int i = 0; i < keys.size(); ++i) {
            String key = keys.get(i);
            boolean lastKey = i == keys.size() - 1;
            ConfigNode configNode = predicates.get(key);
            if (Objects.nonNull(configNode)) {
                if (lastKey) {
                    throw new IllegalStateException("Cannot have two predicate for the same key: " + key);
                }
                predicates = configNode.children;
                continue;
            }
            configNode = lastKey ? predicate : new ConfigNode();
            predicates.put(key, configNode);
        }
        return this;
    }

    public static class Builder {
        private ConfigNode parent;
        
    }
}
