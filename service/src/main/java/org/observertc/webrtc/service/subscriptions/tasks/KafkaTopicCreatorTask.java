package org.observertc.webrtc.service.subscriptions.tasks;

import io.micronaut.configuration.kafka.config.KafkaDefaultConfiguration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Singleton;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.observertc.webrtc.common.jobs.AbstractTask;
import org.observertc.webrtc.service.KafkaTopicsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class KafkaTopicCreatorTask extends AbstractTask {
	private static final Logger logger = LoggerFactory.getLogger(KafkaTopicCreatorTask.class);
	private final KafkaDefaultConfiguration kafkaConfiguration;
	private final KafkaTopicsConfiguration kafkaTopicsConfiguration;

	public KafkaTopicCreatorTask(KafkaDefaultConfiguration configuration, KafkaTopicsConfiguration kafkaTopicsConfiguration) {
		super(KafkaTopicCreatorTask.class.getName());
		this.kafkaConfiguration = configuration;
		this.kafkaTopicsConfiguration = kafkaTopicsConfiguration;
	}

	@Override
	protected void onExecution(Map<String, Map<String, Object>> results) {

		List<String> topicsToCheck = Arrays.asList(
				this.kafkaTopicsConfiguration.observertcReports,
				this.kafkaTopicsConfiguration.webExtrAppSamples
		);
		AdminClient adminClient = AdminClient.create(this.kafkaConfiguration.getConfig());
		Set<String> topics;

		try {
			try {
				topics = adminClient.listTopics().names().get(5000, TimeUnit.MILLISECONDS);
//					topics = adminClient.listTopics().names().get();
			} catch (TimeoutException e) {
				logger.error("Could not get kafka topics by adminclient. The initializer failed");
				return;
			}
			for (Iterator<String> it = topicsToCheck.iterator(); it.hasNext(); ) {
				String topic = it.next();
				logger.info("Checking existence of topic {}", topic);
				if (topics.contains(topic)) {
					continue;
				}
				NewTopic newTopic = new NewTopic(topic, kafkaTopicsConfiguration.partitionNumberOnCreating,
						(short) kafkaTopicsConfiguration.replicationFactorOnCreating);

				final CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(newTopic));
				createTopicsResult.values().get(topic).get();
				logger.info("{} topic is created with replication factor {} and partition number {}", topic,
						kafkaTopicsConfiguration.replicationFactorOnCreating, kafkaTopicsConfiguration.partitionNumberOnCreating);
			}
		} catch (InterruptedException | ExecutionException e) {
			if (!(e.getCause() instanceof TopicExistsException))
				throw new RuntimeException(e.getMessage(), e);
		}

	}

}
