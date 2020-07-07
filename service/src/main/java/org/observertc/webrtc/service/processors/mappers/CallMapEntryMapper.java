package org.observertc.webrtc.service.processors.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.observertc.webrtc.service.model.CallPeerConnectionsEntry;


@SuppressWarnings({"WeakerAccess", "unused"})
public class CallMapEntryMapper implements Serializer<CallPeerConnectionsEntry>, Deserializer<CallPeerConnectionsEntry>, Serde<CallPeerConnectionsEntry> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public void configure(final Map<String, ?> configs, final boolean isKey) {
		OBJECT_MAPPER.registerModule(new JavaTimeModule());
	}

	@SuppressWarnings("unchecked")
	@Override
	public CallPeerConnectionsEntry deserialize(final String topic, final byte[] data) {
		if (data == null) {
			return null;
		}

		try {
			return OBJECT_MAPPER.readValue(data, CallPeerConnectionsEntry.class);
		} catch (final IOException e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public byte[] serialize(final String topic, final CallPeerConnectionsEntry data) {
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
	public Serializer<CallPeerConnectionsEntry> serializer() {
		return this;
	}

	@Override
	public Deserializer<CallPeerConnectionsEntry> deserializer() {
		return this;
	}
}