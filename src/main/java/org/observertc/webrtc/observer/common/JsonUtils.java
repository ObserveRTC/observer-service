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

package org.observertc.webrtc.observer.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

public class JsonUtils {
	private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
	private static final ObjectWriter OBJECT_WRITER;
	private static final ObjectReader OBJECT_READER;
	private static final ObjectMapper OBJECT_MAPPER;

	static {
		OBJECT_WRITER = new ObjectMapper().writer().withDefaultPrettyPrinter();
		OBJECT_READER = new ObjectMapper().reader();
		OBJECT_MAPPER = new ObjectMapper();
	}

	public static String objectToBase64(Object subject) {
		return objectToBase64OrDefault(subject, null);
	}

	public static String beautifyJsonString(String inputJson) {
		try {
			return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(OBJECT_MAPPER.readTree(inputJson));
		} catch (Exception ex) {
			logger.warn("Exception while beautifying json", ex);
			return "";
		}
	}

	public static String objectToBase64OrDefault(Object subject, String defaultValue) {
		if (Objects.isNull(subject)) {
			return defaultValue;
		}
		try {
			byte[] bytes = OBJECT_WRITER.writeValueAsBytes(subject);
			String result = Base64.getEncoder().encodeToString(bytes);
			return result;
		} catch (JsonProcessingException e) {
			logger.warn("Exception occurred while executing method base64ToObject", e);
			return defaultValue;
		}
	}

	public static String objectToString(Object subject) {
		return objectToStringOrDefault(subject, null);
	}

	public static String objectToStringOrDefault(Object subject, String defaultValue) {
		if (Objects.isNull(subject)) {
			return defaultValue;
		}
		try {
			String result = OBJECT_WRITER.writeValueAsString(subject);
			return result;
		} catch (JsonProcessingException e) {
			logger.warn("Exception occurred while executing method base64ToObject", e);
			return defaultValue;
		}
	}

	public static<T> T base64ToObject(String input, Class<T> klass) {
		return base64ToObjectOrDefault(input, klass, null);
	}

	public static<T> T base64ToObjectOrDefault(String input, Class<T> klass, T defaultValue) {
		if (Objects.isNull(input)) {
			return defaultValue;
		}
		try {
			byte[] bytes = Base64.getDecoder().decode(input);
			T result = OBJECT_READER.readValue(bytes, klass);
			return result;
		} catch (IOException e) {
			logger.warn("Exception occurred while executing method base64ToObject", e);
			return defaultValue;
		}
	}

	public static<T> T stringToObject(String subject, Class<T> klass) {
		return stringToObjectOrDefault(subject, klass, null);
	}

	public static<T> T stringToObjectOrDefault(String subject, Class<T> klass, T defaultValue) {
		if (Objects.isNull(subject)) {
			return defaultValue;
		}
		try {
			T result = OBJECT_READER.readValue(subject, klass);
			return result;
		} catch (IOException e) {
			logger.warn("Exception occurred while executing method base64ToObject", e);
			return defaultValue;
		}
	}
}
