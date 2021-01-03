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

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.util.Map;
import java.util.UUID;

/**
 * Filter out every peer connection from the {@link PCState}s, which already exists,
 * register new SSRCs, if it occurs, and forwards all {@link PCState}s which
 * contains unkown peer connection UUID
 */
public interface ActivePCsEvaluator extends Observer<Map<UUID, PCState>> {

	PublishSubject<Map<UUID, PCState>> getNewPeerConnectionsSubject();
}
