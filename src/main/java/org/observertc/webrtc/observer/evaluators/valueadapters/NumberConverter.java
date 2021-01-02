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

package org.observertc.webrtc.observer.evaluators.valueadapters;

import io.micronaut.context.annotation.Prototype;
import java.nio.ByteBuffer;

@Prototype
public class NumberConverter {

	public <T extends Number> Integer toInt(T value) {
		if (value == null) {
			return null;
		}
		return value.intValue();
	}

	public <T extends Number> Short toShort(T value) {
		if (value == null) {
			return null;
		}
		return value.shortValue();
	}

	public byte[] longToBytes(Long value) {
		if (value == null) {
			return null;
		}
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(value);
		return buffer.array();
	}

	public <T extends Number> Double toDouble(T value) {
		if (value == null) {
			return null;
		}
		return value.doubleValue();
	}

	public <T extends Number> Long toLong(T value) {
		if (value == null) {
			return null;
		}
		return value.longValue();
	}

	public <T extends Number> Float toFloat(T value) {
		if (value == null) {
			return null;
		}
		return value.floatValue();
	}

}
