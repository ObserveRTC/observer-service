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

package org.observertc.webrtc.observer;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("evaluators")
public class EvaluatorsConfig {

	public SampleTransformerConfig sampleTransformer;

	public CallCleanerConfig callCleaner;

	public ReportDraftsConfig reportDrafts;

	@ConfigurationProperties("reportDrafts")
	public static class ReportDraftsConfig {
		public int expirationTimeInS = 300;
		public int minEnforcedTimeInS = 20;
	}

	@ConfigurationProperties("callCleaner")
	public static class CallCleanerConfig {
		public int streamMaxIdleTimeInS = 60;
		public int streamMaxAllowedGapInS = 3600;
		public int pcRetentionTimeInDays = 1;

	}

	@ConfigurationProperties("sampleTransformer")
	public static class SampleTransformerConfig {
		public boolean reportOutboundRTPs = true;
		public boolean reportInboundRTPs = true;
		public boolean reportRemoteInboundRTPs = true;
		public boolean reportTracks = true;
		public boolean reportMediaSources = true;
		public boolean reportCandidatePairs = true;
		public boolean reportLocalCandidates = true;
		public boolean reportRemoteCandidates = true;
		public boolean reportUserMediaErrors = true;
	}


}

