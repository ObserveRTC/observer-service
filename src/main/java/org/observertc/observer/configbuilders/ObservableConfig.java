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

package org.observertc.observer.configbuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.IOUtils;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class implements a yaml and json interpreter
 * to provide @link{Map} representation for
 * Observable configurations.
 *
 */
@Singleton
public class ObservableConfig {

	private static final Logger logger = LoggerFactory.getLogger(ObservableConfig.class);

	public Observable<Map<String, Object>> fromYamlString(String input) {
		List<Map<String, Object>> configs = this.makeMapsFromYaml(input);
		return Observable.fromIterable(configs)
				.map(this::fromYamlObject)
				.filter(Objects::nonNull);
	}

	public Observable<Map<String, Object>> fromJsonString(String jsonString) {
		ObjectMapper mapper = new ObjectMapper();
		return Observable.fromCallable(() -> mapper.readValue(
					jsonString, new TypeReference<Map<String, Object>>() {
				}))
				.filter(Objects::nonNull);
	}

	public Observable<Map<String, Object>> fromYamlInputStream(InputStream input) {
		String text = IOUtils.toString(input, StandardCharsets.UTF_8);
		List<Map<String, Object>> configs = this.makeMapsFromYaml(text);
		return Observable.fromIterable(configs)
				.map(this::fromYamlObject)
				.filter(Objects::nonNull);
	}

	private List<Map<String, Object>> makeMapsFromYaml(String input) {
		List<Map<String, Object>> result = new LinkedList<>();
		Yaml yaml = new Yaml(new SafeConstructor());
		Iterable<Object> iterable = yaml.loadAll(input);
		for (Object obj : iterable) {
			if (obj instanceof Map == false) {
				return null;
			}
			Map<String, Object> config = (Map<String, Object>) obj;
			if (config == null) {
				logger.debug("{} does not contain any configuration", input.toString());
				return null;
			}
			result.add(config);
		}
		return result;
	}


	private Map<String, Object> fromYamlObject(Object obj) {
		if (obj instanceof Map == false) {
			return null;
		}
		Map<String, Object> configs = (Map<String, Object>) obj;
		if (configs == null) {
			logger.debug("It does not contain any configuration");
			return null;
		}
		return configs;
	}
}
