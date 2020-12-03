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

package org.observertc.webrtc.observer.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.hazelcast.nio.serialization.ByteArraySerializer;
import java.io.IOException;
import org.observertc.webrtc.observer.models.CallEntity;

public class CallEntitySerializer
		implements ByteArraySerializer<CallEntity> {

	ObjectMapper mapper = new ObjectMapper(new SmileFactory());

	public int getTypeId() {
		return 5;
	}

	public void destroy() {
		
	}

	@Override
	public byte[] write(CallEntity customer) throws IOException {
		return mapper.writeValueAsBytes(customer);
	}

	@Override
	public CallEntity read(byte[] bytes) throws IOException {
		return mapper.readValue(bytes, CallEntity.class);
	}
}
