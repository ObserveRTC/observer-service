package com.observertc.gatekeeper.webrtcstat.processors.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.observertc.gatekeeper.webrtcstat.model.SSRCMapEntry;
import java.io.IOException;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;


@SuppressWarnings({"WeakerAccess", "unused"})
public class SSRCMapEntryMapper implements Serializer<SSRCMapEntry>, Deserializer<SSRCMapEntry>, Serde<SSRCMapEntry> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public void configure(final Map<String, ?> configs, final boolean isKey) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public SSRCMapEntry deserialize(final String topic, final byte[] data) {
		if (data == null) {
			return null;
		}

		try {
			return OBJECT_MAPPER.readValue(data, SSRCMapEntry.class);
		} catch (final IOException e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public byte[] serialize(final String topic, final SSRCMapEntry data) {
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
	public Serializer<SSRCMapEntry> serializer() {
		return this;
	}

	@Override
	public Deserializer<SSRCMapEntry> deserializer() {
		return this;
	}
}