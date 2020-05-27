//package com.observertc.gatekeeper.sinks.micrometer;
//
//import com.observertc.gatekeeper.builders.AbstractBuilder;
//import io.micrometer.core.instrument.Clock;
//import io.micrometer.core.instrument.MeterRegistry;
//import io.micrometer.core.instrument.step.StepRegistryConfig;
//import io.micrometer.elastic.ElasticConfig;
//import io.micronaut.configuration.kafka.KafkaProducerFactory;
//import io.micronaut.context.annotation.Factory;
//import java.util.List;
//import java.util.Map;
//import javax.annotation.Nullable;
//
//@Factory
//public class MicrometerSinkBuilder extends AbstractBuilder {
//
//	public MicrometerSinkBuilder(KafkaProducerFactory kafkaProducerFactory) {
//
//	}
//
//	public void t() {
//		// Build from config
//		StepRegistryConfig registryConfig;
//
//		ElasticConfig elasticConfig = new ElasticConfig() {
//			@Override
//			@Nullable
//			public String get(String k) {
//				return null;
//			}
//		};
//		MeterRegistry registry = new ElasticMeterRegistry(elasticConfig, Clock.SYSTEM);
//	}
//
//	public MicrometerSinkBuilder withConfiguration(Map<String, Object> configuration) {
//		AbstractBuilder.deepMerge(this.getConfigs(), configuration);
//		return this;
//	}
//
//	public static class Config extends AbstractBuilder.Config {
//
//		List<Map<String, Object>> evaluators;
//	}
//}
