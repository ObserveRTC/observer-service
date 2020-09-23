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

package org.observertc.webrtc.reporter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeConverter {

	private static final ZoneId GMT_ZONE_ID = ZoneId.of("GMT");

	public static LocalDateTime epochToGMTLocalDateTime(Long epoch) {
		if (epoch == null) {
			return null;
		}
		Instant epochInstant = Instant.ofEpochMilli(epoch);
		LocalDateTime result = LocalDateTime.ofInstant(epochInstant, GMT_ZONE_ID);
		return result;
	}

	public static Long GMTLocalDateTimeToEpoch(LocalDateTime incomingTs) {
		if (incomingTs == null) {
			return null;
		}
		Long result = incomingTs.atZone(GMT_ZONE_ID).toEpochSecond();
		return result;
	}
}
