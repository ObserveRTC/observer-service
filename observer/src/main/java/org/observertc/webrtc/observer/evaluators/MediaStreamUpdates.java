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

import io.reactivex.rxjava3.core.ObservableOperator;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.UUID;
import org.observertc.webrtc.observer.samples.ObservedPCS;

class MediaStreamUpdates implements ObservableOperator<MediaStreamUpdate, ObservedPCS> {
	private final Scheduler scheduler;
	private final int periodInS;
	private final int buffersNum;
	private final PublishSubject<UUID> expiredPeerConnections = PublishSubject.create();
	private final int pcExpiationTimeInS;

	public MediaStreamUpdates(Scheduler scheduler, int periodInS, int buffersNum, int pcExpiationTimeInS) {
		this.scheduler = scheduler;
		this.periodInS = periodInS;
		this.buffersNum = buffersNum;
		this.pcExpiationTimeInS = pcExpiationTimeInS;
	}

	@Override
	public Observer<? super ObservedPCS> apply(Observer<? super MediaStreamUpdate> observer) throws Exception {
		return new ObservedPCSBuffer(observer, this.scheduler, this.periodInS, this.expiredPeerConnections, this.buffersNum, this.pcExpiationTimeInS);
	}

	public Subject<UUID> getExpiredPCSubject() {
		return this.expiredPeerConnections;
	}
}
