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
@Deprecated
public class TasksProvider {
	@Inject
	Provider<SSRCEntityFinderTask> ssrcEntityTaskProvider;

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
	Provider<CallDetailsFinderTask> callDetailsFinderTaskProvider;

	@Inject
	Provider<PeerConnectionDetailsFinderTask> peerConnectionDetailsFinderTaskProvider;

	public TasksProvider() {

	}

	public SSRCEntityFinderTask getSSRCFinderTask() {
		return this.ssrcEntityTaskProvider.get();
	}

	public CallInitializerTask getCallInitializerTask() {
		return this.callInitializerTaskProvider.get();
	}

	public CallFinisherTask getCallFinisherTask() {
		return this.callFinisherTaskProvider.get();
	}

	public PeerConnectionsUpdaterTask getPeerConnectionsUpdaterTask() {
		return this.peerConnectionsUpdaterTaskProvider.get();
	}

	public PeerConnectionJoinerTask getPeerConnectionJoinerTask() {
		return this.peerConnectionJoinerTaskProvider.get();
	}

	public PeerConnectionDetacherTask getPeerConnectionDetacherTask() {
		return this.peerConnectionDetacherTaskProvider.get();
	}

	public PeerConnectionsFinderTask getPeerConnectionFinderTask() {
		return this.peerConnectionsFinderTaskProvider.get();
	}

	public CallDetailsFinderTask getCallDetailsFinderTask() {
		return this.callDetailsFinderTaskProvider.get();
	}

	public PeerConnectionDetailsFinderTask getPeerConnectionDetailsFinderTask() {
		return this.peerConnectionDetailsFinderTaskProvider.get();
	}

}