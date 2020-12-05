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

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class TasksProvider {
	private final Provider<CallFinderTask> callFinderTaskProvider;
	private final Provider<CallFinisherTask> callFinisherTaskProvider;
	private final Provider<CallInitializerTask> callInitializerTaskProvider;
	private final Provider<PeerConnectionJoinerTask> peerConnectionJoinerTaskProvider;
	private final Provider<PeerConnectionDetacherTask> peerConnectionDetacherTaskProvider;
	private final Provider<PeerConnectionsUpdaterTask2> peerConnectionsUpdaterTaskProvider;

	public TasksProvider(Provider<CallFinderTask> callFinderTaskProvider,
						 Provider<CallFinisherTask> callFinisherTaskProvider,
						 Provider<CallInitializerTask> callInitializerTaskProvider,
						 Provider<PeerConnectionJoinerTask> peerConnectionJoinerTaskProvider,
						 Provider<PeerConnectionDetacherTask> peerConnectionDetacherTaskProvider,
						 Provider<PeerConnectionsUpdaterTask2> peerConnectionsUpdaterTaskProvider
	) {
		this.callFinderTaskProvider = callFinderTaskProvider;
		this.peerConnectionDetacherTaskProvider = peerConnectionDetacherTaskProvider;
		this.callFinisherTaskProvider = callFinisherTaskProvider;
		this.callInitializerTaskProvider = callInitializerTaskProvider;
		this.peerConnectionJoinerTaskProvider = peerConnectionJoinerTaskProvider;
		this.peerConnectionsUpdaterTaskProvider = peerConnectionsUpdaterTaskProvider;
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

	public PeerConnectionsUpdaterTask2 providePeerConnectionsUpdaterTask() {
		return this.peerConnectionsUpdaterTaskProvider.get();
	}

	public PeerConnectionJoinerTask providePeerConnectionJoinerTask() {
		return this.peerConnectionJoinerTaskProvider.get();
	}

	public PeerConnectionDetacherTask providePeerConnectionDetacherTask() {
		return this.peerConnectionDetacherTaskProvider.get();
	}

}