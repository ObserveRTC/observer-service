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

package org.observertc.webrtc.observer.samples;

import org.observertc.webrtc.observer.common.Utils;

import java.util.UUID;

public class SourceSample {

	public UUID serviceUUID = null;
	public String mediaUnitId = null;
	public UUID peerConnectionUUID = null;
	public Object sample = null;

	public static class Builder {
		private SourceSample result = new SourceSample();

		public Builder withServiceUUID(UUID serviceUUID) {
			this.result.serviceUUID = serviceUUID;
			return this;
		}

		public Builder withPeerConnectionUUID(UUID peerConnectionUUID) {
			this.result.peerConnectionUUID = peerConnectionUUID;
			return this;
		}

		public Builder withMediaUnitId(String mediaUnitId) {
			this.result.mediaUnitId = mediaUnitId;
			return this;
		}

		public Builder withSample(Object sample) {
			this.result.sample = sample;
			return this;
		}

		public SourceSample build() {
			if (Utils.anyNull(this.result.serviceUUID, this.result.sample)) {
				throw new RuntimeException("Cannot convert to sourcesample");
			}
			return result;
		}
	}
}
