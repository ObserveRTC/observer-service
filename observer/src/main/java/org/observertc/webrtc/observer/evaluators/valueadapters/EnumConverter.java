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

import javax.inject.Singleton;
import org.observertc.webrtc.schemas.reports.CandidateType;
import org.observertc.webrtc.schemas.reports.ICEState;
import org.observertc.webrtc.schemas.reports.MediaType;
import org.observertc.webrtc.schemas.reports.NetworkType;
import org.observertc.webrtc.schemas.reports.RTCQualityLimitationReason;
import org.observertc.webrtc.schemas.reports.TransportProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EnumConverter {

	private static final Logger logger = LoggerFactory.getLogger(EnumConverter.class);

	public <S extends Enum> MediaType toReportMediaType(S source) {
		if (source == null) {
			return MediaType.NULL;
		}
		String sourceStr = source.name();
		try {
			MediaType result = MediaType.valueOf(sourceStr);
			return result;
		} catch (Exception ex) {
			logger.error("Cannot convert from {} of {} to {} so it is UNKNOWN result", sourceStr, source.getClass().getName(),
					MediaType.class.getName());
			return MediaType.UNKNOWN;
		}
	}

	public <S extends Enum> RTCQualityLimitationReason toQualityLimitationReason(S source) {
		if (source == null) {
			return RTCQualityLimitationReason.NULL;
		}
		String sourceStr = source.name();
		try {
			RTCQualityLimitationReason result = RTCQualityLimitationReason.valueOf(sourceStr);
			return result;
		} catch (Exception ex) {
			logger.error("Cannot convert from {} of {} to {} so it is UNKNOWN result", sourceStr, source.getClass().getName(),
					RTCQualityLimitationReason.class.getName());
			return RTCQualityLimitationReason.UNKNOWN;
		}
	}

	public <S extends Enum> CandidateType toCandidateType(S source) {
		if (source == null) {
			return CandidateType.NULL;
		}
		String sourceStr = source.name();
		try {
			CandidateType result = CandidateType.valueOf(sourceStr);
			return result;
		} catch (Exception ex) {
			logger.error("Cannot convert from {} of {} to {} so it is UNKNOWN result", sourceStr, source.getClass().getName(),
					CandidateType.class.getName());
			return CandidateType.UNKNOWN;
		}
	}

	public <S extends Enum> TransportProtocol toInternetProtocol(S source) {
		if (source == null) {
			return TransportProtocol.NULL;
		}
		String sourceStr = source.name();
		try {
			TransportProtocol result = TransportProtocol.valueOf(sourceStr);
			return result;
		} catch (Exception ex) {
			logger.error("Cannot convert from {} of {} to {} so it is UNKNOWN result", sourceStr, source.getClass().getName(),
					TransportProtocol.class.getName());
			return TransportProtocol.UNKNOWN;
		}
	}

	public <S extends Enum> NetworkType toNetworkType(S source) {
		if (source == null) {
			return NetworkType.NULL;
		}
		String sourceStr = source.name();
		try {
			NetworkType result = NetworkType.valueOf(sourceStr);
			return result;
		} catch (Exception ex) {
			logger.error("Cannot convert from {} of {} to {} so it is UNKNOWN result", sourceStr, source.getClass().getName(),
					NetworkType.class.getName());
			return NetworkType.UNKNOWN;
		}
	}

	public <S extends Enum> ICEState toICEState(S source) {
		if (source == null) {
			return ICEState.NULL;
		}
		String sourceStr = source.name();
		try {
			ICEState result = ICEState.valueOf(sourceStr);
			return result;
		} catch (Exception ex) {
			logger.error("Cannot convert from {} of {} to {} so it is UNKNOWN result", sourceStr, source.getClass().getName(),
					ICEState.class.getName());
			return ICEState.UNKNOWN;
		}
	}

}
