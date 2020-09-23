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

	Map<String, Map<String, Object>> getProfiles();

	IConfigurationProfiles withProfiles(Map<String, Object> profles);

	IConfigurationProfiles using(IConfigurationLoader configurationLoader);

	IConfigurationLoader getConfigurationLoader();
}
