package org.observertc.webrtc.common.builders;

import java.util.Map;

/**
 * An interface for any kind of Builder class
 */
public interface IBuilderAbstract<T> {
	/**
	 * Sets up the Builder imlementation with the provided configuration
	 *
	 * @param configs The provided configuration used to build the result storage
	 * @return {@link IBuilderAbstract} to configure the Builder further.
	 */
	T withConfiguration(Map<String, Object> configs);

	/**
	 * Sets up the value for a configuration provided in the name field.
	 *
	 * @param key   the key of the attribute we want to change. if it is in an embedded map, use "." to navigate to it.
	 *              for exanmple: configuration.capacty will navigate to the capacity attribute inside the configuration.
	 * @param value the value we want to set
	 * @return {@link this} to configure the builder further
	 */
	T withConfiguration(String key, Object value);

	/**
	 * Gets the configuration for a key. If a key contains dot("."), then it tries to navigate to the embedded
	 *
	 * @param key the key of the configuration we want to retrueve.
	 * @return The object it retrieves.
	 */
	Object getConfiguration(String key);

	/**
	 * Sets up the storageprofiles, which provides the parsed profiles for
	 * using the using keyword in building.
	 *
	 * @param profiles the instantiated AbstractProfiles
	 * @return {@link IBuilderAbstract} to configure the Builder further.
	 */
	T withProfiles(IConfigurationProfiles profiles);

}
