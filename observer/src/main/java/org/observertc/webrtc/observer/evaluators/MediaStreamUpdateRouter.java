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

import java.util.UUID;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Peer connection handled by this instance of the observer
 */
@Singleton
public final class MediaStreamUpdateRouter {
	private static final Logger logger = LoggerFactory.getLogger(MediaStreamUpdateRouter.class);
	private static final int MEDIA_STREAM_UPDATER_NUM = 4;

	private final MediaStreamUpdater[] mediaStreamUpdaters;

	public MediaStreamUpdateRouter(
			Provider<MediaStreamUpdater> mediaStreamUpdaterProvider
	) {
		this.mediaStreamUpdaters = new MediaStreamUpdater[MEDIA_STREAM_UPDATER_NUM];
		for (int i = 0; i < MEDIA_STREAM_UPDATER_NUM; ++i) {
			MediaStreamUpdater mediaStreamUpdater = mediaStreamUpdaterProvider.get();
			mediaStreamUpdater.setName(String.format("mediaStreamUpdater-%d", i));
			this.mediaStreamUpdaters[i] = mediaStreamUpdater;
		}
	}


	public void add(ObservedPCS observedPCS) {
		UUID peerConnectionUUID = observedPCS.peerConnectionUUID;
		int index = Math.abs(peerConnectionUUID.hashCode()) % MEDIA_STREAM_UPDATER_NUM;
		this.mediaStreamUpdaters[index].add(observedPCS);
	}
}