package org.observertc.webrtc.service.subscriptions;

import io.micronaut.configuration.kafka.config.KafkaDefaultConfiguration;
import javax.inject.Singleton;
import org.observertc.webrtc.common.jobs.Job;
import org.observertc.webrtc.service.KafkaTopicsConfiguration;
import org.observertc.webrtc.service.subscriptions.tasks.KafkaTopicCreatorTask;

@Singleton
public class KafkaInitializerJob extends Job {

	public KafkaInitializerJob(KafkaTopicCreatorTask topicCreatorTask, KafkaDefaultConfiguration configuration, KafkaTopicsConfiguration kafkaTopicsConfiguration) {
		this.withDescription("Kafka Initializer will check everything necessary for kafka")
				.withTask(topicCreatorTask);
	}
}
