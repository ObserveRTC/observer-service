package org.observertc.webrtc.service.processors.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.observertc.webrtc.service.samples.ObserveRTCCIceStatsSample;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ObserveRTCICEStatsSampleMapper implements Serializer<ObserveRTCCIceStatsSample>, Deserializer<ObserveRTCCIceStatsSample>, Serde<ObserveRTCCIceStatsSample> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public void configure(final Map<String, ?> configs, final boolean isKey) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObserveRTCCIceStatsSample deserialize(final String topic, final byte[] data) {
		if (data == null) {
			return null;
		}

		try {
			return OBJECT_MAPPER.readValue(data, ObserveRTCCIceStatsSample.class);
		} catch (final IOException e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public byte[] serialize(final String topic, final ObserveRTCCIceStatsSample data) {
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
	public Serializer<ObserveRTCCIceStatsSample> serializer() {
		return this;
	}

	@Override
	public Deserializer<ObserveRTCCIceStatsSample> deserializer() {
		return this;
	}
}