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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationProfiles implements IConfigurationProfiles {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationProfiles.class);

	private IConfigurationLoader loader;
	private Map<String, Map<String, Object>> profiles = new HashMap<>();

	public ConfigurationProfiles() {

	}

	/**
	 * Gets configuration for the provided profile, if it exists
	 *
	 * @param profileKey The key for the profile the configuration belongs to.
	 * @return A {@link Map} with the corresponding configurations, or null
	 */
	public Map<String, Object> getConfigurationFor(String profileKey) {
		String[] navigated = profileKey.split("\\.", 2);
		if (navigated.length < 2) {
			return this.profiles.get(profileKey);
		}
		if (this.loader == null) {
			// well, we don't have options here.
			return this.profiles.get(profileKey);
		}
		// we need to look for sourcekey
		String sourceKey = navigated[0];
		IConfigurationProfiles profiles = this.loader.getConfigurationSourceFor(sourceKey);
		if (profiles == null) {
			return null;
		}
		String innerProfileKey = navigated[1];
		return profiles.getConfigurationFor(innerProfileKey);
	}

	@Override
	public Map<String, Map<String, Object>> getProfiles() {
		return this.profiles;
	}

	/**
	 * @param profiles
	 * @return
	 */
	public IConfigurationProfiles withProfiles(Map<String, Object> profiles) {
		for (Iterator<Map.Entry<String, Object>> it = profiles.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, Object> entry = it.next();
			String profileKey = entry.getKey();
			if (entry.getValue() instanceof Map == false) {
				logger.debug("Found a non map profilesSource");
				continue;
			}
			Map<String, Object> profilesSource = (Map<String, Object>) entry.getValue();
			this.profiles.put(profileKey, profilesSource);
		}
		return this;
	}

	@Override
	public IConfigurationProfiles using(IConfigurationLoader configurationLoader) {
		this.loader = configurationLoader;
		return this;
	}

	@Override
	public IConfigurationLoader getConfigurationLoader() {
		return this.loader;
	}
}
