package org.observertc.webrtc.observer.evaluators.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;


@SuppressWarnings({"WeakerAccess", "unused"})
public class JsonToPOJOMapper<T> implements Serializer<T>, Deserializer<T>, Serde<T> {
	static {
		OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
	}

	private static final ObjectMapper OBJECT_MAPPER;
	private final Class<T> klass;

	public JsonToPOJOMapper(Class<T> klass) {
		this.klass = klass;
	}

	@Override
	public void configure(final Map<String, ?> configs, final boolean isKey) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public T deserialize(final String topic, final byte[] data) {
		if (data == null) {
			return null;
		}

		try {
			return OBJECT_MAPPER.readValue(data, this.klass);
		} catch (final IOException e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public byte[] serialize(final String topic, final T data) {
		if (data == null) {
			return null;
		}

		try {
			return OBJECT_MAPPER.writeValueAsBytes(data);
		} catch (final Exception e) {
			throw new SerializationException("Error serializing JSON message", e);
		}
	}

	@Override
	public void close() {
	}

	@Override
	public Serializer<T> serializer() {
		return this;
	}

	@Override
	public Deserializer<T> deserializer() {
		return this;
	}
}