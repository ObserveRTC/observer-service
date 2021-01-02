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

package org.observertc.webrtc.observer.repositories.hazelcast;

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class RepositoryProvider {
	private final Provider<CallPeerConnectionsRepository> callPeerConnectionsRepositoryProvider;
	private final Provider<CallNamesRepository> callNamesRepositoryProvider;
	private final Provider<CallEntitiesRepository> callEntitiesRepositoryProvider;
	private final Provider<PeerConnectionsRepository> peerConnectionsRepositoryProvider;
	private final Provider<SynchronizationSourcesRepository> SSRCRepositoryProvider;
	private final Provider<CallSynchronizationSourcesRepository> callSynchronozationSourcesRepositoryProvider;

	public RepositoryProvider(
			Provider<CallEntitiesRepository> callEntitiesRepositoryProvider,
			Provider<CallNamesRepository> callNamesRepositoryProvider,
			Provider<CallPeerConnectionsRepository> callPeerConnectionsRepositoryProvider,
			Provider<PeerConnectionsRepository> peerConnectionsRepositoryProvider,
			Provider<SynchronizationSourcesRepository> SSRCRepositoryProvider,
			Provider<CallSynchronizationSourcesRepository> callSynchronozationSourcesRepositoryProvider
	) {
		this.callNamesRepositoryProvider = callNamesRepositoryProvider;
		this.callPeerConnectionsRepositoryProvider = callPeerConnectionsRepositoryProvider;
		this.callEntitiesRepositoryProvider = callEntitiesRepositoryProvider;
		this.peerConnectionsRepositoryProvider = peerConnectionsRepositoryProvider;
		this.SSRCRepositoryProvider = SSRCRepositoryProvider;
		this.callSynchronozationSourcesRepositoryProvider = callSynchronozationSourcesRepositoryProvider;
	}

	public SynchronizationSourcesRepository getSSRCRepository() {
		return this.SSRCRepositoryProvider.get();
	}

	public CallEntitiesRepository getCallEntitiesRepository() {
		return this.callEntitiesRepositoryProvider.get();
	}

	public PeerConnectionsRepository getPeerConnectionsRepository() {
		return this.peerConnectionsRepositoryProvider.get();
	}

	public CallPeerConnectionsRepository getCallPeerConnectionsRepository() {
		return this.callPeerConnectionsRepositoryProvider.get();
	}

	public CallNamesRepository getCallNamesRepository() {
		return this.callNamesRepositoryProvider.get();
	}

	public CallSynchronizationSourcesRepository getCallSynchronizationSourcesRepository() {
		return this.callSynchronozationSourcesRepositoryProvider.get();
	}
}
