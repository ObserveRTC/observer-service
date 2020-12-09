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

import io.micronaut.context.annotation.Replaces;
import org.observertc.webrtc.observer.ObserverHazelcast;
import org.observertc.webrtc.observer.repositories.hazelcast.RepositoryProvider;
import org.observertc.webrtc.observer.tasks.CallFinderTask;
import org.observertc.webrtc.observer.tasks.TasksProvider;

public class MockTaskProvider extends TasksProvider {
	public MockTaskProvider() {
		super(null, // CallFinder
				null, // CallFinisher
				null, // CallInitializer
				null, // PeerConnectionJoiner
				null, // PeerConnectionDetacher
				null // PeerConnectionUpdater
		);
	}

	@Replaces(CallFinderTask.class)
	private class MockCallFinderTask extends CallFinderTask {

		public MockCallFinderTask(ObserverHazelcast observerHazelcast, RepositoryProvider repositoryProvider) {
			super(observerHazelcast, repositoryProvider);
		}
	}


}
