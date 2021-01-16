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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolationException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	private static Logger logger = LoggerFactory.getLogger(AbstractBuilder.class);
	private static final String BUILDER_CLASS_SUFFIX = "Builder";

	public static String getBuilderClassName( String className) {
		if (!className.endsWith(BUILDER_CLASS_SUFFIX)){
			return className.concat(BUILDER_CLASS_SUFFIX);
		}
		return className;
	}

	private final Map<String, Object> config = new HashMap<>();

	/**
	 * Gets a klass corresponding to the name of the class
	 *
	 * @param className the name of the class
	 * @param <T>       the type of the class
	 * @return the class type
	 * @throws RuntimeException if the type of the klass does not exists
	 */
	protected <T> Optional<Class<T>> getClassFor(String className) {
		Class<T> result = null;
		List<String> classes = new LinkedList<>();
		classes.add(className);

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
			return Optional.empty();
		}

		return Optional.of(result);
	}

	protected<T> Optional<T> tryInvoke(String className, Object... params) {
		T result = this.invoke(className, params);
		if (Objects.nonNull(result)) {
			return Optional.of(result);
		}

		Package thisPackage = this.getClass().getPackage();
		return this.tryInvoke(thisPackage, className, params);

	}

	protected<T> Optional<T> tryInvoke(Package startPackage, String className, Object... params) {
		T result = this.invoke(className, params);
		if (Objects.nonNull(result)) {
			return Optional.of(result);
		}

		List<String> packages = Arrays.stream(Package.getPackages())
				.filter(p -> p.getName().startsWith(startPackage.getName()))
				.map(Package::getName)
				.collect(Collectors.toList());

		for (String packageName : packages) {
			String klassName = String.join(".", packageName, className);
			result = this.invoke(klassName, params);
			if (Objects.nonNull(result)) {
				return Optional.of(result);
			}
		}
		return Optional.empty();
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

		Optional<Class<T>> klassHolder = this.getClassFor(className);
		if (!klassHolder.isPresent()) {
			return null;
		}
		Class<T> klass = klassHolder.get();

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

	protected <T> T convertAndValidate(Class<T> klass) {
		return ConfigConverter.convert(klass, this.config);
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
		return ConfigConverter.convert(klass, configs);
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
		Object value = this.config.get(key);
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
		this.config.put(key, value);
	}

	/**
	 * Sets up the value for a configuration provided in the name field.
	 *
	 * @param key   the key of the attribute we want to change. if it is in an embedded map, use "." to navigate to it.
	 *              for exanmple: configuration.capacty will navigate to the capacity attribute inside the configuration.
	 * @param value the value we want to set
	 * @return {@link this} to configure the builder further
	 */
	public void withConfiguration(String key, Object value) {
		this.config.put(key, value);
	}

	public void withConfiguration(Map<String, Object> source) {
		if (Objects.isNull(source)) {
			return;
		}
		if (source.size() < 1) {
			return;
		}
		this.getConfig().putAll(source);
	}

	protected Map<String, Object> getConfig() {
		return this.config;
	}

	public Object getConfiguration(String key) {
		return this.config.get(key);
	}

}
