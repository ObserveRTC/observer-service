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
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Base64;
import java.util.Objects;

public class JsonUtils {
	private static final ObjectWriter OBJECT_WRITER;

	static {
		OBJECT_WRITER = new ObjectMapper().writer().withDefaultPrettyPrinter();
	}

	public static String objectToBase64(Object subject) {
		if (Objects.isNull(subject)) {
			return null;
		}
		try {
			byte[] bytes = OBJECT_WRITER.writeValueAsBytes(subject);
			String result = Base64.getEncoder().encodeToString(bytes);
			return result;
		} catch (JsonProcessingException e) {
			return e.getMessage();
		}
	}

	public static String objectToString(Object subject) {
		if (Objects.isNull(subject)) {
			return "null";
		}
		try {
			String result = OBJECT_WRITER.writeValueAsString(subject);
			return result;
		} catch (JsonProcessingException e) {
			return e.getMessage();
		}
	}
}
