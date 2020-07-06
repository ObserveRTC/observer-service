package org.observertc.webrtc.observer.service.builders;

import java.util.Map;

/**
 * An interface to collect configurations for elements need to be built
 */
public interface IConfigurationProfiles {

	/**
	 * Gets storage based on profile key added as configuration.
	 *
	 * @param profileKey The key for the profile the storage configuration belongs to.
	 */
	Map<String, Object> getConfigurationFor(String profileKey);

	IConfigurationProfiles withProfiles(Map<String, Object> profles);

	IConfigurationProfiles using(IConfigurationLoader configurationLoader);

	IConfigurationLoader getConfigurationLoader();
}
