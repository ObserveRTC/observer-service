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

package org.observertc.webrtc.common.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a skeletal implementation for builder classes
 * to minimize the effort required to implement a builder
 *
 * <p>To implement any kind of builder, it is recommended to extend this class
 * and use the {@link this#convertAndValidate(Class)} method, which validates and
 * converts the provided configuration (which is a {@link Map<String, Object>} type)
 * to the desired class, and throws {@link ConstraintViolationException} if
 * a validation fails.
 *
 * <p>The programmer should generally provide the configuration keys in the
 * extended class.
 *
 * @author Balazs Kreith
 * @since 0.1
 */
public abstract class AbstractBuilder {

	private static final String CONFIG_KEY_BUILDER = "builder";

	private static final String CONFIG_KEY_CONFIGURATION = "configuration";

	private static final String CONFIG_KEY_USING = "using";

	private static final String SYSTEM_ENV_PATTERN_REGEX = "\\$\\{([A-Za-z0-9_]+)(?::([^\\}]*))?\\}";
	private final Pattern systemEnvPattern = Pattern.compile(SYSTEM_ENV_PATTERN_REGEX);
	private static Logger logger = LoggerFactory.getLogger(AbstractBuilder.class);

	private ObjectMapper mapper = new ObjectMapper();
	private IConfigurationProfiles profiles;
	private Map<String, Object> configs = new HashMap<>();


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

	/**
	 * Constructs an abstract builder
	 */
	public AbstractBuilder() {

	}

	protected <T> T convertAndValidate(Class<T> klass) {
		// a comment, to not to let the IDE make it in one line
		return this.convertAndValidate(klass, this.configs);
	}

	/**
	 * Converts the provided configuration to the type of object provided as a parameter, and
	 * validates the conversion.
	 *
	 * @param klass The type of the object we want to convert the configuration to
	 * @param <T>   The type of the result we return after the conversion
	 * @return An object of the desired type setup with values from the configuration.
	 * @throws ConstraintViolationException if the validation fails during the conversion.
	 */
	protected <T> T convertAndValidate(Class<T> klass, Map<String, Object> configs) {
		this.checkForSystemEnv(configs);
		T result = this.mapper.convertValue(configs, klass);
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<T>> violations = validator.validate(result);

		if (violations != null && !violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<T> constraintViolation : violations) {
				sb.append(constraintViolation.getMessage())
						.append(" ");
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

	protected Object checkForSystemEnv(Object obj) {
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
				entry.setValue(after);
			}
			return map;
		}
		return obj;
	}


	public <T> T get(String key) {
		return this.get(key, obj -> (T) obj);
	}

	/**
	 * Gets the config belongs to the key, and if it exists, it
	 * converts it using a converter function provided in the params.
	 * If the key does not exist it returns null.
	 *
	 * @param key       The key we are looking for in the so far provided configurations
	 * @param converter The converter converts to the desired type of object if the key exists
	 * @param <T>       The type of the result of the conversion
	 * @return The result of the convert operation if the key exists, null otherwise
	 */
	protected <T> T get(String key, Function<Object, T> converter) {
		return this.getOrDefault(key, converter, null);
	}

	/**
	 * Gets the config belongs to the key, and if it exists, it
	 * converts it using a converter function provided in the params.
	 * If the key does not exist it returns the defaultValue.
	 *
	 * @param key          The key we are looking for in the so far provided configurations
	 * @param converter    The converter converts to the desired type of object if the key exists
	 * @param defaultValue The default value returned if the key does not exist
	 * @param <T>          The type of the result of the conversion
	 * @return The result of the convert operation if the key exists, defaultValue otherwise
	 */
	protected <T> T getOrDefault(String key, Function<Object, T> converter, T defaultValue) {
		Object value = this.configs.get(key);
		if (value == null) {
			return defaultValue;
		}
		T result = converter.apply(value);
		return result;
	}

	/**
	 * Adds a key - value pair to the configuration map
	 *
	 * @param key   The key we bound the value to
	 * @param value The value we store for the corresponding key
	 */
	protected void configure(String key, Object value) {
		this.configs.put(key, value);
	}

	/**
	 * Gets a klass corresponding to the name of the class
	 *
	 * @param className the name of the class
	 * @param <T>       the type of the class
	 * @return the class type
	 * @throws RuntimeException if the type of the klass does not exists
	 */
	protected <T> Class<T> getClassFor(String className) {
		Class<T> result = null;
		List<String> classes = new LinkedList<>();
		classes.add(className);

		IConfigurationProfiles profiles = this.getProfiles();
		if (profiles != null) {
			IConfigurationLoader loader = profiles.getConfigurationLoader();
			if (loader != null) {
				loader.getPackageResolvers().stream().forEach(packageName -> classes.add(
						packageName.concat(".").concat(className
						)));
			}
		}
		// first let's try with resolver
		for (Iterator<String> it = classes.iterator(); it.hasNext(); ) {
			String candidateName = it.next();
			Class<T> candidate;
			try {
				candidate = (Class<T>) Class.forName(candidateName);
			} catch (ClassNotFoundException e) {
				continue;
			}

			if (result != null && result.getName().equals(candidate.getName()) == false) {
				throw new RuntimeException("Duplicated class found for "
						.concat(className).concat(": ").concat(result.getName()).concat(" and ").concat(
								candidate.getName()
						));
			}
			result = candidate;
		}

		if (result == null) {
			throw new RuntimeException("Class type for " + className + " does not exist");
		}

		return result;
	}

	/**
	 * Invokes a constructor for the given class
	 *
	 * @param className the name of the class
	 * @param params    the parameters given to the constructor when we invokes it
	 * @param <T>       the type of the class
	 * @return An instantiated object with a type of {@link T}.
	 * @throws RuntimeException if there was a problem in invocation
	 */
	protected <T> T invoke(String className, Object... params) {

		Class<T> klass = this.getClassFor(className);

		Constructor<T> constructor;
		try {
			constructor = klass.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("No constructor exists which accept () for type " + klass.getName(), e);
		}
		Object constructed;
		try {
			constructed = constructor.newInstance(params);
		} catch (InstantiationException e) {
			throw new RuntimeException("Error by invoking constructor for type " + klass.getName(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error by invoking constructor for type " + klass.getName(), e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error by invoking constructor for type " + klass.getName(), e);
		}
		return (T) constructed;
	}


	protected Map<String, Object> getConfigs() {
		return this.configs;
	}

	public Object getConfiguration(String key) {
		return this.configs.get(key);
	}

	/**
	 * @return
	 */
	protected IConfigurationProfiles getProfiles() {
		return this.profiles;
	}

	protected void setProfiles(IConfigurationProfiles profiles) {
		this.profiles = profiles;
	}

	/**
	 * Build the configuration map for this builder
	 *
	 * @return
	 */
	protected Map<String, Object> buildConfigurations() {
		return this.buildConfigurations(this.configs);
	}

	/**
	 * Gets the configuration, in which it merges the profile backwards
	 *
	 * @param source the sourcemap, which will be exemined for profile key
	 * @return the result map, which is merged using profile keys
	 */
	protected Map<String, Object> buildConfigurations(Map<String, Object> source) {
		if (source.get(CONFIG_KEY_USING) == null) {
			return source;
		}
		Map<String, Object> actual = new HashMap<>();
		actual.putAll(this.configs);
		String profileKey = (String) actual.remove(CONFIG_KEY_USING);
		IConfigurationProfiles profiles = this.getProfiles();
		Objects.requireNonNull(profiles);
		Map<String, Object> profileConfiguration = profiles.getConfigurationFor(profileKey);
		Map<String, Object> result = deepMerge(profileConfiguration, actual);
		return this.buildConfigurations(result);
	}

	public static class Config {
		/**
		 * The name of the builder class
		 */
		public String builder;

		/**
		 * The specific configurations
		 */
		public Map<String, Object> configuration;

		/**
		 * The used profile if we have
		 */
		public String using;
	}
}
