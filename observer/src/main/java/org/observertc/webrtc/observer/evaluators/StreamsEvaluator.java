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

import io.micronaut.scheduling.TaskExecutors;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import org.observertc.webrtc.observer.ReportSink;
import org.observertc.webrtc.observer.StreamsEvaluatorsConfig;
import org.observertc.webrtc.observer.samples.ObservedPCS;

@Singleton
public class StreamsEvaluator implements Observer<ObservedPCS> {
	private final PublishSubject<ObservedPCS> subject = PublishSubject.create();
	private final StreamsEvaluatorsConfig config;

	public StreamsEvaluator(
			@Named(TaskExecutors.MESSAGE_CONSUMER) ExecutorService executorService,
			StreamsEvaluatorsConfig config,
			ActivePCsEvaluator activePCsEvaluator,
			ExpiredPCsEvaluator expiredPCsEvaluator,
			ReportDraftsEvaluator reportDraftsEvaluator,
			ReportSink reportSink
	) {
		this.config = config;
		// Construct the routes
		Scheduler scheduler = Schedulers.from(executorService);
		MediaStreamUpdates buffer = new MediaStreamUpdates(
				scheduler,
				config.mediaStreamUpdatesFlushInS,
				config.mediaStreamsBufferNums,
				config.peerConnectionMaxIdleTimeInS
		);

		subject
				.lift(buffer)
				.subscribeOn(scheduler)
				.buffer(this.config.mediaStreamsBufferDebounceTimeInMs, TimeUnit.MILLISECONDS)
				.subscribe(activePCsEvaluator)
		;


		buffer.getExpiredPCSubject()
				.subscribeOn(scheduler)
				.buffer(this.config.mediaStreamsBufferDebounceTimeInMs, TimeUnit.MILLISECONDS)
				.subscribe(expiredPCsEvaluator);

		activePCsEvaluator
				.getInitiatedCallSubject()
//				.toSerialized()
				.subscribe(reportDraftsEvaluator);

		activePCsEvaluator
				.getJoinedPeerConnectionSubject()
				.subscribe(reportSink);

		expiredPCsEvaluator
				.getFinishedCallSubject()
				.subscribe(reportDraftsEvaluator);

		expiredPCsEvaluator
				.getDetachedPeerConnections()
				.subscribe(reportSink);
	}


	@Override
	public void onSubscribe(@NonNull Disposable d) {

	}

	@Override
	public void onNext(@NonNull ObservedPCS observedPCS) {
		this.subject.onNext(observedPCS);
	}

	@Override
	public void onError(@NonNull Throwable e) {

	}

	@Override
	public void onComplete() {

	}
}
