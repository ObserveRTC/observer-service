package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;

@JsonDeserialize(using = Timestamp.Deserializer.class)
@JsonSerialize(using = Timestamp.Serializer.class)
public class Timestamp {
	public Double doubleValue;
	public String stringValue;

	static class Deserializer extends JsonDeserializer<Timestamp> {
		@Override
		public Timestamp deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
			Timestamp value = new Timestamp();
			switch (jsonParser.currentToken()) {
				case VALUE_NUMBER_INT:
				case VALUE_NUMBER_FLOAT:
					value.doubleValue = jsonParser.readValueAs(Double.class);
					break;
				case VALUE_STRING:
					String string = jsonParser.readValueAs(String.class);
					value.stringValue = string;
					break;
				default: throw new IOException("Cannot deserialize Timestamp");
			}
			return value;
		}
	}

	static class Serializer extends JsonSerializer<Timestamp> {
		@Override
		public void serialize(Timestamp obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			if (obj.doubleValue != null) {
				jsonGenerator.writeObject(obj.doubleValue);
				return;
			}
			if (obj.stringValue != null) {
				jsonGenerator.writeObject(obj.stringValue);
				return;
			}
			throw new IOException("Timestamp must not be null");
		}
	}
}
