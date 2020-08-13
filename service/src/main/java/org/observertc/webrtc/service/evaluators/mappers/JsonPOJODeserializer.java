package org.observertc.webrtc.service.evaluators.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class JsonPOJODeserializer<T> implements Deserializer<T> {
	private ObjectMapper objectMapper = new ObjectMapper();

	private final Class<T> tClass;

	/**
	 * Default constructor needed by Kafka
	 */
	public JsonPOJODeserializer(Class<T> tClass) {
		this.tClass = tClass;
	}

	@Override
	public T deserialize(String topic, byte[] bytes) {
		if (bytes == null)
			return null;

		T data;
		try {
			data = objectMapper.readValue(bytes, tClass);
		} catch (Exception e) {
			throw new SerializationException(e);
		}

		return data;
	}

	public T deserialize(String sample) {
		if (sample == null)
			return null;

		T data;
		try {
			data = objectMapper.readValue(sample, tClass);
		} catch (Exception e) {
			throw new SerializationException(e);
		}

		return data;
	}

	@Override
	public void close() {

	}
}