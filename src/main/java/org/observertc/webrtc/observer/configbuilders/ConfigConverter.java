/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.observer.configbuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.*;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class converts {@link Map<String, Object>} config to a target class.
 * Additionally before the validation it tries to match system env variables if it is given to supply values.
 * The target class will be validated with {@link javax.validation.ConstraintViolation}, so it should been
 * wired to an implementation like hybernite
 *
 * @author Balazs Kreith
 * @since 0.1
 */
public class ConfigConverter<T> implements Function<Map<String, Object>, T>{
	private static final String SYSTEM_ENV_PATTERN_REGEX = "\\$\\{([A-Za-z0-9_]+)(?::([^\\}]*))?\\}";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(ConfigConverter.class);
	private final Pattern systemEnvPattern = Pattern.compile(SYSTEM_ENV_PATTERN_REGEX);

	public static<R> R convert(Class<R> klass, Map<String, Object> config) {
		return new ConfigConverter<R>(klass).apply(config);
	}

	public static Map<String, Object> convertToMap(Object source) {
        byte[] bytes;
        try {
            bytes = OBJECT_MAPPER.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            logger.warn("{} Cannot convert to bytes", source.toString());
            return Collections.emptyMap();
        }
        Map<String, Object> result;
        try {
            result = OBJECT_MAPPER.readValue(bytes, Map.class);
        } catch (IOException e) {
            logger.warn("{} Cannot convert to map", source.toString());
            return Collections.emptyMap();
        }
        return result;
    }
	/**
	 * @param original the original map the merge place to
	 * @param newMap   the newmap we merge to the original one
	 * @return the original map extended by the newMap
	 * @see <a href="https://stackoverflow.com/questions/25773567/recursive-merge-of-n-level-maps">source</a>
	 */
	public static Map deepMerge(Map original, Map newMap) {
		if (newMap == null) {
			return original;
		} else if (original == null) {
			original = new HashMap();
		}
		for (Object key : newMap.keySet()) {
			if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
				Map originalChild = (Map) original.get(key);
				Map newChild = (Map) newMap.get(key);
				original.put(key, deepMerge(originalChild, newChild));
			} else if (newMap.get(key) instanceof List && original.get(key) instanceof List) {
				List originalChild = (List) original.get(key);
				List newChild = (List) newMap.get(key);
				for (Object each : newChild) {
					if (!originalChild.contains(each)) {
						originalChild.add(each);
					}
				}
			} else {
				original.put(key, newMap.get(key));
			}
		}
		return original;
	}


	public static Map<String, Object> flatten(Map<String, Object> structured, String delimiter) {
		Map<String, Object> result = new HashMap<>();
		Iterator<Map.Entry<String, Object>> it = structured.entrySet().iterator();
		for (; it.hasNext(); ) {
			Map.Entry<String, Object> entry = it.next();
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Map == false) {
				result.put(key, value);
				continue;
			}
			flatten((Map<String, Object>) value, delimiter)
					.entrySet()
					.stream()
					.forEach(flattenEntry ->
							result.put(key.concat(delimiter).concat(flattenEntry.getKey()), flattenEntry.getValue()));
		}
		return result;
	}

	public static void forceKeysToBeCamelCase(Map<String, Object> map) {
		Map<String, Object> copy = new HashMap<>(map);
		Iterator<Map.Entry<String, Object>> it = copy.entrySet().iterator();
		for (; it.hasNext(); ) {
			Map.Entry<String, Object> entry = it.next();
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Map == false) {
				String[] parts = key.split("-");
				if (1 < parts.length) {
					String newKey = parts[0];
					for (int i = 1; i < parts.length; ++i) {
						String part = parts[i];
						// If you have your hair pulled out because of a
						// config bug you have not found the reason
						// why it does not work, and in the end you
						// find yourself here, and it turns out
						// you wanted to use Uuid, but its always UUID, then
						//
						// I am sorry!
						//
						if (part.equals("uuid")) {
							newKey += "UUID";
						} else {
							newKey += part.substring(0, 1).toUpperCase() + part.substring(1);
						}
					}
					map.remove(key);
					map.put(newKey, value);
				}
				continue;
			}
			forceKeysToBeCamelCase((Map<String, Object>) value);
		}
	}

	private final Class<T> klass;

	/**
	 * Constructs an abstract builder
	 */
	public ConfigConverter(Class<T> klass) {
		this.klass = klass;
	}

	public T apply(Map<String, Object> configs) {
		// a comment, to not to let the IDE make it in one line
		return this.convertAndValidate(klass, configs);
	}

	/**
	 * Converts the provided configuration to the type of object provided as a parameter, and
	 * validates the conversion.
	 *
	 * @param klass The type of the object we want to convert the configuration to
	 * @return An object of the desired type setup with values from the configuration.
	 * @throws ConstraintViolationException if the validation fails during the conversion.
	 */
	private T convertAndValidate(Class<T> klass, Map<String, Object> configs) {
		this.checkForSystemEnv(configs);
		T result = this.OBJECT_MAPPER.convertValue(configs, klass);
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<T>> violations = validator.validate(result);

		if (violations != null && !violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<T> constraintViolation : violations) {
				sb.append(constraintViolation.getMessage()).append(" ");
			}

			String errorMessage = sb.toString();

			if (logger.isDebugEnabled()) {
				logger.debug(errorMessage);
			}

			throw new ConstraintViolationException(violations);
		}
		return result;
	}

	/**
	 * Checks all values for a configuration may possible have a pattern points to system environments.
	 * If it does found a pattern: ${ENV} or ${ENV:DEFAULT_VALUE} than it tries to get a
	 * system variable name ENV (case sensitive try!), if it does not find it,
	 * it checks if a DEFAULT_VALUE has been set, and assign that.
	 * <p>
	 * NOTE: ${HOST:localhost:1} sets the DEFAULT_VALUE to "localhost:1", but for
	 * IDE parsing reason, or feeling better whatever, you can write `localhost:1`, the
	 * result will be the same.
	 */

	private String convertValue(String value) {
		Matcher matcher = this.systemEnvPattern.matcher(value);

		while (matcher.find()) {
			String ENV = matcher.group(1);
			String envValue = System.getenv(ENV);
			if (envValue == null) {
				envValue = matcher.group(2);
				if (envValue != null) {
					char quote = '`';
					if (envValue.charAt(0) == quote && envValue.charAt(envValue.length() - 1) == quote) {
						envValue = envValue.substring(1, envValue.length() - 1);
					}
				} else {
					// It is necessary to assign an empty striing when nothing has been found
					// because otherwise the subexpr would crash with null.
					envValue = "";
				}
			}
			Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
			value = subexpr.matcher(value).replaceAll(envValue);
		}
		return value;
	}

	private Object checkForSystemEnv(Object obj) {
		if (obj instanceof String) {
			return this.convertValue((String) obj);
		}
		if (obj instanceof List) {
			List subject = ((List) obj);
			for (int i = 0; i < subject.size(); ++i) {
				Object before = subject.get(i);
				Object after = this.checkForSystemEnv(before);
				if (!before.equals(after)) {
					subject.set(i, after);
				}
			}
			return subject;
		}
		if (obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
			for (; it.hasNext(); ) {
				Map.Entry<String, Object> entry = it.next();
				Object before = entry.getValue();
				Object after = this.checkForSystemEnv(before);
				if (Objects.isNull(before)) {
					if (Objects.nonNull(after)) {
						entry.setValue(after);
					}
				} else if (!before.equals(after)){
					entry.setValue(after);
				}
			}
			return map;
		}
		return obj;
	}
}
