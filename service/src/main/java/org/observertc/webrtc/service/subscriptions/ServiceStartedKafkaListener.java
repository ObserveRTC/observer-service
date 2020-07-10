package org.observertc.webrtc.service.subscriptions;

import io.micronaut.configuration.kafka.config.KafkaDefaultConfiguration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceStartedEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Singleton;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.observertc.webrtc.service.KafkaTopicsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class ServiceStartedKafkaListener implements ApplicationEventListener<ServiceStartedEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceStartedKafkaListener.class);
	private final KafkaDefaultConfiguration kafkaConfiguration;
	private final Stage<AdminClient> pipeline;
	private final KafkaTopicsConfiguration kafkaTopicsConfiguration;
	private volatile boolean run = false;

	public ServiceStartedKafkaListener(KafkaDefaultConfiguration configuration, KafkaTopicsConfiguration kafkaTopicsConfiguration) {
		this.kafkaConfiguration = configuration;
		this.kafkaTopicsConfiguration = kafkaTopicsConfiguration;
		this.pipeline = new StagePipelineBuilder<AdminClient>()
				.withStage(this::makeTopicCreator)
				.getFirst();
	}

	private Stage<AdminClient> makeTopicCreator() {
		List<String> topicsToCheck = Arrays.asList(
				this.kafkaTopicsConfiguration.observerSSRCPeerConnectionSamples,
				this.kafkaTopicsConfiguration.observeRTCCIceStatsSample,
				this.kafkaTopicsConfiguration.observeRTCMediaStreamStatsSamples,
				this.kafkaTopicsConfiguration.observertcReports
		);
		return new Stage<AdminClient>() {
			@Override
			public void accept(AdminClient adminClient) {
				try {
					Set<String> topics;
					try {
						topics = adminClient.listTopics().names().get(5000, TimeUnit.MILLISECONDS);
//					topics = adminClient.listTopics().names().get();
					} catch (TimeoutException e) {
						logger.error("Could not get kafka topics by adminclient. The initializer failed");
						return;
					}
					for (Iterator<String> it = topicsToCheck.iterator(); it.hasNext(); ) {
						String topic = it.next();
						logger.debug("Checking existence of topic {}", topic);
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
				super.accept(adminClient);
			}
		};
	}

	@Override
	public void onApplicationEvent(ServiceStartedEvent event) {
		if (this.run) {
			return;
		}
		this.run = true;
		logger.info("Kafka Initializer checking is started");
		AdminClient adminClient = AdminClient.create(this.kafkaConfiguration.getConfig());
		this.pipeline.accept(adminClient);
	}
}
