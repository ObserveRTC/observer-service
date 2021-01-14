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

package org.observertc.webrtc.observer.tasks;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class TasksProvider {
	@Inject
	Provider<CallFinderTask> callFinderTaskProvider;

	@Inject
	Provider<CallFinisherTask> callFinisherTaskProvider;

	@Inject
	Provider<CallInitializerTask> callInitializerTaskProvider;

	@Inject
	Provider<PeerConnectionJoinerTask> peerConnectionJoinerTaskProvider;

	@Inject
	Provider<PeerConnectionDetacherTask> peerConnectionDetacherTaskProvider;

	@Inject
	Provider<PeerConnectionsUpdaterTask> peerConnectionsUpdaterTaskProvider;

	@Inject
	Provider<PeerConnectionsFinderTask> peerConnectionsFinderTaskProvider;

	@Inject
	Provider<ICEConnectionAdderTask> iceConnectionAdderTaskProvider;

	@Inject
	Provider<ICEConnectionRemoverTask> iceConnectionRemoverTaskProvider;

	public TasksProvider() {

	}

	public CallFinderTask getCallFinderTask() {
		return this.callFinderTaskProvider.get();
	}

	public CallInitializerTask provideCallInitializerTask() {
		return this.callInitializerTaskProvider.get();
	}

	public CallFinisherTask provideCallFinisherTask() {
		return this.callFinisherTaskProvider.get();
	}

	public PeerConnectionsUpdaterTask providePeerConnectionsUpdaterTask() {
		return this.peerConnectionsUpdaterTaskProvider.get();
	}

	public PeerConnectionJoinerTask providePeerConnectionJoinerTask() {
		return this.peerConnectionJoinerTaskProvider.get();
	}

	public PeerConnectionDetacherTask providePeerConnectionDetacherTask() {
		return this.peerConnectionDetacherTaskProvider.get();
	}

	public PeerConnectionsFinderTask providePeerConnectionFinderTask() {
		return this.peerConnectionsFinderTaskProvider.get();
	}

	public ICEConnectionAdderTask provideICEConnectionAdderTask() {
		return this.iceConnectionAdderTaskProvider.get();
	}

	public ICEConnectionRemoverTask provideICEConnectionRemoverTask() {
		return this.iceConnectionRemoverTaskProvider.get();
	}

}