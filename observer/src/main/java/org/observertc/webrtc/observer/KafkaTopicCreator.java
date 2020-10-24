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

import io.micronaut.configuration.kafka.config.KafkaDefaultConfiguration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class KafkaTopicCreator
		// implements ApplicationEventListener<KafkaTopicCheckEvent> 
{
	private static final Logger logger = LoggerFactory.getLogger(KafkaTopicCreator.class);
	private final KafkaDefaultConfiguration kafkaConfiguration;
	private final boolean createIfNotExists;
	private final boolean runAdminClient;
	private final Set<String> checkedTopics;

	public KafkaTopicCreator(KafkaDefaultConfiguration configuration, KafkaTopicsConfiguration kafkaTopicsConfiguration) {
		this.kafkaConfiguration = configuration;
		this.checkedTopics = Collections.newSetFromMap(new ConcurrentHashMap<>());
		this.createIfNotExists = kafkaTopicsConfiguration.createIfNotExists;
		this.runAdminClient = kafkaTopicsConfiguration.runAdminClient;
	}

	//	@Override
//	@Async
	public void execute(KafkaTopicsConfiguration.TopicConfig topicConfig) {
		if (this.checkedTopics.contains(topicConfig.topicName)) {
			logger.info("{} topic is already checked", topicConfig.topicName);
			return;
		}
		if (!this.runAdminClient) {
			this.checkedTopics.add(topicConfig.topicName);
			logger.info("AdminClient is not allowed to run");
			return;
		}
		AdminClient adminClient = AdminClient.create(this.kafkaConfiguration.getConfig());
		Set<String> topics;
		try {
			topics = adminClient.listTopics().names().get(5000, TimeUnit.MILLISECONDS);
//					topics = adminClient.listTopics().names().get();
		} catch (Exception e) {
			logger.error("Could not get kafka topics by adminclient. The initializer failed", e);
			return;
		} finally {
			adminClient.close();
		}

		this.checkedTopics.add(topicConfig.topicName);

		try {
			logger.info("Checking existence of topic {}", topicConfig.topicName);
			if (topics.contains(topicConfig.topicName)) {
				return;
			}
			if (!this.createIfNotExists) {
				logger.warn("Topic {} does not exists, but {} cannot create it, because createIfNotExists is set to false",
						topicConfig.topicName, this.getClass().getName());
				return;
			}
			NewTopic newTopic = new NewTopic(topicConfig.topicName, topicConfig.onCreatePartitionNums,
					(short) topicConfig.onCreateReplicateFactor);
			Map<String, String> topicConfigs = new HashMap<>();
			topicConfigs.put("retention.ms", Long.toString(topicConfig.retentionTimeInMs));
			newTopic.configs(topicConfigs);

			final CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(newTopic));
			createTopicsResult.values().get(topicConfig.topicName).get();
			logger.info("{} topic is created with replication factor {} and partition number {} retention time in ms {}",
					topicConfig.topicName,
					topicConfig.onCreateReplicateFactor,
					topicConfig.onCreatePartitionNums,
					topicConfig.retentionTimeInMs);


		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error at creating instance", e);
			if (!(e.getCause() instanceof TopicExistsException))
				throw new RuntimeException(e.getMessage(), e);
		} finally {
			adminClient.close();
		}
	}
}