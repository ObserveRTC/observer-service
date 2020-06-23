package com.observertc.gatekeeper.webrtcstat.processors.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.observertc.gatekeeper.webrtcstat.model.CallMapEntry;
import java.io.IOException;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;


@SuppressWarnings({"WeakerAccess", "unused"})
public class CallMapEntryMapper implements Serializer<CallMapEntry>, Deserializer<CallMapEntry>, Serde<CallMapEntry> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public void configure(final Map<String, ?> configs, final boolean isKey) {
		OBJECT_MAPPER.registerModule(new JavaTimeModule());
	}

	@SuppressWarnings("unchecked")
	@Override
	public CallMapEntry deserialize(final String topic, final byte[] data) {
		if (data == null) {
			return null;
		}

		try {
			return OBJECT_MAPPER.readValue(data, CallMapEntry.class);
		} catch (final IOException e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public byte[] serialize(final String topic, final CallMapEntry data) {
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
	public Serializer<CallMapEntry> serializer() {
		return this;
	}

	@Override
	public Deserializer<CallMapEntry> deserializer() {
		return this;
	}
}