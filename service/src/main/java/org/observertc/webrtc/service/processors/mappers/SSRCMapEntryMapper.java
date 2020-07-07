package org.observertc.webrtc.service.processors.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.observertc.webrtc.service.model.PeerConnectionSSRCsEntry;


@SuppressWarnings({"WeakerAccess", "unused"})
public class SSRCMapEntryMapper implements Serializer<PeerConnectionSSRCsEntry>, Deserializer<PeerConnectionSSRCsEntry>, Serde<PeerConnectionSSRCsEntry> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public void configure(final Map<String, ?> configs, final boolean isKey) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public PeerConnectionSSRCsEntry deserialize(final String topic, final byte[] data) {
		if (data == null) {
			return null;
		}

		try {
			return OBJECT_MAPPER.readValue(data, PeerConnectionSSRCsEntry.class);
		} catch (final IOException e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public byte[] serialize(final String topic, final PeerConnectionSSRCsEntry data) {
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
	public Serializer<PeerConnectionSSRCsEntry> serializer() {
		return this;
	}

	@Override
	public Deserializer<PeerConnectionSSRCsEntry> deserializer() {
		return this;
	}
}