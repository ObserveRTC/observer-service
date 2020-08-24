package org.observertc.webrtc.observer.subscriptions.tasks;

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
import org.observertc.webrtc.observer.KafkaTopicsConfiguration;
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
		List<KafkaTopicsConfiguration.TopicConfig> topicConfigs = Arrays.asList(
				this.kafkaTopicsConfiguration.observertcReports,
				this.kafkaTopicsConfiguration.webExtrAppSamples,
				this.kafkaTopicsConfiguration.observertcReportDrafts
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
			for (Iterator<KafkaTopicsConfiguration.TopicConfig> it = topicConfigs.iterator(); it.hasNext(); ) {
				KafkaTopicsConfiguration.TopicConfig topicConfig = it.next();
				logger.info("Checking existence of topic {}", topicConfig.topicName);
				if (topics.contains(topicConfig.topicName)) {
					continue;
				}
				NewTopic newTopic = new NewTopic(topicConfig.topicName, topicConfig.onCreatePartitionNums,
						(short) topicConfig.onCreateReplicateFactor);

				final CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(newTopic));
				createTopicsResult.values().get(topicConfig.topicName).get();
				logger.info("{} topic is created with replication factor {} and partition number {}",
						topicConfig.topicName,
						topicConfig.onCreateReplicateFactor,
						topicConfig.onCreatePartitionNums);
			}
		} catch (InterruptedException | ExecutionException e) {
			if (!(e.getCause() instanceof TopicExistsException))
				throw new RuntimeException(e.getMessage(), e);
		}

	}

}
