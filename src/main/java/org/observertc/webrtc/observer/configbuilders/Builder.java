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

import java.util.Map;

/**
 * An interface for any kind of Builder class
 */
public interface Builder<T> {
	/**
	 * Sets up the Builder imlementation with the provided configuration
	 *
	 * @param configs The provided configuration used to build the result storage
	 */
	void withConfiguration(Map<String, Object> configs);

	/**
	 * Gets the configuration for a key. If a key contains dot("."), then it tries to navigate to the embedded
	 *
	 * @param key the key of the configuration we want to retrueve.
	 * @return The object it retrieves.
	 */
	Object getConfiguration(String key);

	<S extends T> S build();

}
