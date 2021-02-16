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

package org.observertc.webrtc.observer.evaluators;

import org.jeasy.random.EasyRandom;
import org.observertc.webrtc.observer.entities.OldCallEntity;
import org.observertc.webrtc.observer.entities.OldPeerConnectionEntity;
import org.observertc.webrtc.observer.entities.SynchronizationSourceEntity;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.observertc.webrtc.schemas.reports.ICELocalCandidate;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;

import java.util.*;

public class TestInputsGenerator {

	private final EasyRandom generator;

	public TestInputsGenerator() {
		this.generator = new EasyRandom();
	}

	public ObservedPCS makeObservedPCS() {
		return this.generator.nextObject(ObservedPCS.class);
	}

	public PCState makePCState() {
		return this.generator.nextObject(PCState.class);
	}

	public PCState makePCStateFor(OldPeerConnectionEntity pcEntity, SynchronizationSourceEntity... ssrcEntities) {
		PCState result = this.makePCState();
		result.serviceUUID = pcEntity.serviceUUID;
		result.callName = pcEntity.callName;
		result.browserId = pcEntity.browserId;
		result.mediaUnitID = pcEntity.mediaUnitId;
		result.serviceName = pcEntity.serviceName;
		result.peerConnectionUUID = pcEntity.peerConnectionUUID;
		result.timeZoneId = pcEntity.timeZone;
		result.marker = pcEntity.marker;
		result.userId = pcEntity.providedUserName;
		if (Objects.nonNull(ssrcEntities)) {
			Arrays.stream(ssrcEntities).map(e -> e.SSRC).forEach(result.SSRCs::add);
		}

		return result;
	}

	public OldPeerConnectionEntity makePeerConnectionEntity() {
		return this.generator.nextObject(OldPeerConnectionEntity.class);
	}

	public SynchronizationSourceEntity makeSynchronizationSourceEntity() {
		return this.generator.nextObject(SynchronizationSourceEntity.class);
	}

	public Report generateICELocalCandidateReport() {
		var payload = generator.nextObject(ICELocalCandidate.class);
		return this.generateReport(ReportType.ICE_LOCAL_CANDIDATE, payload);
	}

	public Report generateICERemoteCandidateReport() {
		var payload = generator.nextObject(ICELocalCandidate.class);
		return this.generateReport(ReportType.ICE_REMOTE_CANDIDATE, payload);
	}

	public Report generateICECandidatePairReport() {
		var payload = generator.nextObject(ICELocalCandidate.class);
		return this.generateReport(ReportType.ICE_CANDIDATE_PAIR, payload);
	}

	private Report generateReport(ReportType type, Object payload) {
		return Report.newBuilder()
				.setVersion(1)
				.setType(type)
				.setTimestamp(1L)
				.setServiceUUID(UUID.randomUUID().toString())
				.setServiceName("serviceName")
				.setMarker("marker")
				.setPayload(payload)
				.build();
	}

	public OldPeerConnectionEntity makePeerConnectionEntityFor(SynchronizationSourceEntity ssrcEntity) {
		OldPeerConnectionEntity result = this.makePeerConnectionEntity();
		result.callUUID = ssrcEntity.callUUID;
		result.serviceUUID = ssrcEntity.serviceUUID;
		return result;
	}

	public PCState makePCStateFor(ObservedPCS observedPCS) {
		return this.generator.nextObject(PCState.class);
	}


	public static TestInputsGenerator.Builder builder() {
		return new TestInputsGenerator.Builder();
	}

	public OldCallEntity makeCallEntityFor(OldPeerConnectionEntity... pcEntities) {
		OldCallEntity result = this.makeCallEntity();
		if (Objects.isNull(pcEntities) || pcEntities.length < 1) {
			return result;
		}
		OldPeerConnectionEntity pcEntity = pcEntities[0];
		result.serviceName = pcEntity.serviceName;
		result.serviceUUID = pcEntity.serviceUUID;
		result.callName = pcEntity.callName;
		result.callUUID = pcEntity.callUUID;
		result.marker = pcEntity.marker;
		result.initiated = Arrays.stream(pcEntities).map(e -> e.joined).min(Long::compare).get();
		return result;
	}

	private OldCallEntity makeCallEntity() {
		return generator.nextObject(OldCallEntity.class);
	}


	public static class Builder {
		private final Map<String, Object> values;

		public Builder() {
			this.values = new HashMap<>();
		}

		public TestInputsGenerator build() {
			return new TestInputsGenerator();
		}
	}


}
