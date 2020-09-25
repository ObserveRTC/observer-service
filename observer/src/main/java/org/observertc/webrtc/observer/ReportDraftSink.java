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

package org.observertc.webrtc.observer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.inject.Singleton;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.observertc.webrtc.observer.evaluators.reportdrafts.ReportDraft;

@Singleton
public class ReportDraftSink {
	private final Producer<UUID, ReportDraft> reportDraftProducer;
	private final KafkaTopicsConfiguration config;

	public ReportDraftSink(
			KafkaTopicsConfiguration config,
			@KafkaClient("reportDrafts") Producer<UUID, ReportDraft> reportDraftProducer
	) {
		this.config = config;
		this.reportDraftProducer = reportDraftProducer;
	}

	public Future<RecordMetadata> send(UUID peerConnectionUUID, ReportDraft reportDraft) {
		return this.reportDraftProducer.send(new ProducerRecord<UUID, ReportDraft>(this.config.reportDrafts.topicName, peerConnectionUUID,
				reportDraft));
//		return null;
	}

}
