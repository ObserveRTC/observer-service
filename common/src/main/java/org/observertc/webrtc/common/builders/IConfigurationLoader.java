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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * An interface for a loader holds configuration from different sources
 */
public interface IConfigurationLoader {

	Map<String, IConfigurationProfiles> getConfigurationSources();

	IConfigurationProfiles getConfigurationSourceFor(String sourceKey);

	IConfigurationLoader withYaml(InputStream input);

	IConfigurationLoader withProfiles(String sourceKey, IConfigurationProfiles profiles);

	IConfigurationLoader mergeWith(IConfigurationLoader peer);

	IConfigurationLoader withPackageResolvers(List<String> packageResolvers);

	List<String> getPackageResolvers();
}
