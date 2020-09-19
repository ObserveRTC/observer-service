package org.observertc.webrtc.observer.evaluators;

import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Prototype;
import java.util.UUID;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.KafkaSinks;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KafkaListener(
		groupId = "observertc-webrtc-observer-ObservedPCSEvaluator",
		threads = 4
)
@Prototype
public class ObservedPCSEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ObservedPCSEvaluator.class);
	private final EvaluatorsConfig.SampleTransformerConfig config;
	private final SentReportsChecker sentReportsChecker;

	private final KafkaSinks kafkaSinks;

	public ObservedPCSEvaluator(EvaluatorsConfig.SampleTransformerConfig config,
								SentReportsChecker sentReportsChecker,
								KafkaSinks kafkaSinks) {
		this.config = config;
		this.sentReportsChecker = sentReportsChecker;
		this.kafkaSinks = kafkaSinks;
	}


	@Topic("${kafkaTopics.observedPCS.topicName}")
	public void receive(@KafkaKey UUID peerConnectionUUID, ObservedPCS sample) {
		
	}

}
