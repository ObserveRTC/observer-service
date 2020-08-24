package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class Converter {
	// Date-time helpers

	private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
			.appendOptional(DateTimeFormatter.ISO_DATE_TIME)
			.appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
			.appendOptional(DateTimeFormatter.ISO_INSTANT)
			.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SX"))
			.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX"))
			.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
			.toFormatter()
			.withZone(ZoneOffset.UTC);

	public static OffsetDateTime parseDateTimeString(String str) {
		return ZonedDateTime.from(Converter.DATE_TIME_FORMATTER.parse(str)).toOffsetDateTime();
	}

	private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
			.appendOptional(DateTimeFormatter.ISO_TIME)
			.appendOptional(DateTimeFormatter.ISO_OFFSET_TIME)
			.parseDefaulting(ChronoField.YEAR, 2020)
			.parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
			.parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
			.toFormatter()
			.withZone(ZoneOffset.UTC);

	public static OffsetTime parseTimeString(String str) {
		return ZonedDateTime.from(Converter.TIME_FORMATTER.parse(str)).toOffsetDateTime().toOffsetTime();
	}
	// Serialize/deserialize helpers

	public static MediaSource MediaSourceFromJsonString(String json) throws IOException {
		return getMediaSourceObjectReader().readValue(json);
	}

	public static String MediaSourceToJsonString(MediaSource obj) throws JsonProcessingException {
		return getMediaSourceObjectWriter().writeValueAsString(obj);
	}

	public static CandidatePair CandidatePairFromJsonString(String json) throws IOException {
		return getCandidatePairObjectReader().readValue(json);
	}

	public static String CandidatePairToJsonString(CandidatePair obj) throws JsonProcessingException {
		return getCandidatePairObjectWriter().writeValueAsString(obj);
	}

	public static RemoteCandidate RemoteCandidateFromJsonString(String json) throws IOException {
		return getRemoteCandidateObjectReader().readValue(json);
	}

	public static String RemoteCandidateToJsonString(RemoteCandidate obj) throws JsonProcessingException {
		return getRemoteCandidateObjectWriter().writeValueAsString(obj);
	}

	public static LocalCandidate LocalCandidateFromJsonString(String json) throws IOException {
		return getLocalCandidateObjectReader().readValue(json);
	}

	public static String LocalCandidateToJsonString(LocalCandidate obj) throws JsonProcessingException {
		return getLocalCandidateObjectWriter().writeValueAsString(obj);
	}

	public static ICECandidate ICECandidateFromJsonString(String json) throws IOException {
		return getICECandidateObjectReader().readValue(json);
	}

	public static String ICECandidateToJsonString(ICECandidate obj) throws JsonProcessingException {
		return getICECandidateObjectWriter().writeValueAsString(obj);
	}

	public static Track TrackFromJsonString(String json) throws IOException {
		return getTrackObjectReader().readValue(json);
	}

	public static String TrackToJsonString(Track obj) throws JsonProcessingException {
		return getTrackObjectWriter().writeValueAsString(obj);
	}

	public static OutboundRTP OutboundRTPFromJsonString(String json) throws IOException {
		return getOutboundRTPObjectReader().readValue(json);
	}

	public static String OutboundRTPToJsonString(OutboundRTP obj) throws JsonProcessingException {
		return getOutboundRTPObjectWriter().writeValueAsString(obj);
	}

	public static InboundRTP InboundRTPFromJsonString(String json) throws IOException {
		return getInboundRTPObjectReader().readValue(json);
	}

	public static String InboundRTPToJsonString(InboundRTP obj) throws JsonProcessingException {
		return getInboundRTPObjectWriter().writeValueAsString(obj);
	}

	public static RemoteInboundRTP RemoteInboundRTPFromJsonString(String json) throws IOException {
		return getRemoteInboundRTPObjectReader().readValue(json);
	}

	public static String RemoteInboundRTPToJsonString(RemoteInboundRTP obj) throws JsonProcessingException {
		return getRemoteInboundRTPObjectWriter().writeValueAsString(obj);
	}

	public static ObserveRTCCIceStats ObserveRTCCIceStatsFromJsonString(String json) throws IOException {
		return getObserveRTCCIceStatsObjectReader().readValue(json);
	}

	public static String ObserveRTCCIceStatsToJsonString(ObserveRTCCIceStats obj) throws JsonProcessingException {
		return getObserveRTCCIceStatsObjectWriter().writeValueAsString(obj);
	}

	public static ObserveRTCStats ObserveRTCStatsFromJsonString(String json) throws IOException {
		return getObserveRTCStatsObjectReader().readValue(json);
	}

	public static String ObserveRTCStatsToJsonString(ObserveRTCStats obj) throws JsonProcessingException {
		return getObserveRTCStatsObjectWriter().writeValueAsString(obj);
	}

	public static PeerConnectionSample PeerConnectionSampleFromJsonString(String json) throws IOException {
		return getPeerConnectionSampleObjectReader().readValue(json);
	}

	public static String PeerConnectionSampleToJsonString(PeerConnectionSample obj) throws JsonProcessingException {
		return getPeerConnectionSampleObjectWriter().writeValueAsString(obj);
	}

	private static ObjectReader MediaSourceReader;
	private static ObjectWriter MediaSourceWriter;

	private static void instantiateMediaSourceMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		MediaSourceReader = mapper.readerFor(MediaSource.class);
		MediaSourceWriter = mapper.writerFor(MediaSource.class);
	}

	private static ObjectReader getMediaSourceObjectReader() {
		if (MediaSourceReader == null) instantiateMediaSourceMapper();
		return MediaSourceReader;
	}

	private static ObjectWriter getMediaSourceObjectWriter() {
		if (MediaSourceWriter == null) instantiateMediaSourceMapper();
		return MediaSourceWriter;
	}

	private static ObjectReader CandidatePairReader;
	private static ObjectWriter CandidatePairWriter;

	private static void instantiateCandidatePairMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		CandidatePairReader = mapper.readerFor(CandidatePair.class);
		CandidatePairWriter = mapper.writerFor(CandidatePair.class);
	}

	private static ObjectReader getCandidatePairObjectReader() {
		if (CandidatePairReader == null) instantiateCandidatePairMapper();
		return CandidatePairReader;
	}

	private static ObjectWriter getCandidatePairObjectWriter() {
		if (CandidatePairWriter == null) instantiateCandidatePairMapper();
		return CandidatePairWriter;
	}

	private static ObjectReader RemoteCandidateReader;
	private static ObjectWriter RemoteCandidateWriter;

	private static void instantiateRemoteCandidateMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		RemoteCandidateReader = mapper.readerFor(RemoteCandidate.class);
		RemoteCandidateWriter = mapper.writerFor(RemoteCandidate.class);
	}

	private static ObjectReader getRemoteCandidateObjectReader() {
		if (RemoteCandidateReader == null) instantiateRemoteCandidateMapper();
		return RemoteCandidateReader;
	}

	private static ObjectWriter getRemoteCandidateObjectWriter() {
		if (RemoteCandidateWriter == null) instantiateRemoteCandidateMapper();
		return RemoteCandidateWriter;
	}

	private static ObjectReader LocalCandidateReader;
	private static ObjectWriter LocalCandidateWriter;

	private static void instantiateLocalCandidateMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		LocalCandidateReader = mapper.readerFor(LocalCandidate.class);
		LocalCandidateWriter = mapper.writerFor(LocalCandidate.class);
	}

	private static ObjectReader getLocalCandidateObjectReader() {
		if (LocalCandidateReader == null) instantiateLocalCandidateMapper();
		return LocalCandidateReader;
	}

	private static ObjectWriter getLocalCandidateObjectWriter() {
		if (LocalCandidateWriter == null) instantiateLocalCandidateMapper();
		return LocalCandidateWriter;
	}

	private static ObjectReader ICECandidateReader;
	private static ObjectWriter ICECandidateWriter;

	private static void instantiateICECandidateMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		ICECandidateReader = mapper.readerFor(ICECandidate.class);
		ICECandidateWriter = mapper.writerFor(ICECandidate.class);
	}

	private static ObjectReader getICECandidateObjectReader() {
		if (ICECandidateReader == null) instantiateICECandidateMapper();
		return ICECandidateReader;
	}

	private static ObjectWriter getICECandidateObjectWriter() {
		if (ICECandidateWriter == null) instantiateICECandidateMapper();
		return ICECandidateWriter;
	}

	private static ObjectReader TrackReader;
	private static ObjectWriter TrackWriter;

	private static void instantiateTrackMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		TrackReader = mapper.readerFor(Track.class);
		TrackWriter = mapper.writerFor(Track.class);
	}

	private static ObjectReader getTrackObjectReader() {
		if (TrackReader == null) instantiateTrackMapper();
		return TrackReader;
	}

	private static ObjectWriter getTrackObjectWriter() {
		if (TrackWriter == null) instantiateTrackMapper();
		return TrackWriter;
	}

	private static ObjectReader OutboundRTPReader;
	private static ObjectWriter OutboundRTPWriter;

	private static void instantiateOutboundRTPMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		OutboundRTPReader = mapper.readerFor(OutboundRTP.class);
		OutboundRTPWriter = mapper.writerFor(OutboundRTP.class);
	}

	private static ObjectReader getOutboundRTPObjectReader() {
		if (OutboundRTPReader == null) instantiateOutboundRTPMapper();
		return OutboundRTPReader;
	}

	private static ObjectWriter getOutboundRTPObjectWriter() {
		if (OutboundRTPWriter == null) instantiateOutboundRTPMapper();
		return OutboundRTPWriter;
	}

	private static ObjectReader InboundRTPReader;
	private static ObjectWriter InboundRTPWriter;

	private static void instantiateInboundRTPMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		InboundRTPReader = mapper.readerFor(InboundRTP.class);
		InboundRTPWriter = mapper.writerFor(InboundRTP.class);
	}

	private static ObjectReader getInboundRTPObjectReader() {
		if (InboundRTPReader == null) instantiateInboundRTPMapper();
		return InboundRTPReader;
	}

	private static ObjectWriter getInboundRTPObjectWriter() {
		if (InboundRTPWriter == null) instantiateInboundRTPMapper();
		return InboundRTPWriter;
	}

	private static ObjectReader RemoteInboundRTPReader;
	private static ObjectWriter RemoteInboundRTPWriter;

	private static void instantiateRemoteInboundRTPMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		RemoteInboundRTPReader = mapper.readerFor(RemoteInboundRTP.class);
		RemoteInboundRTPWriter = mapper.writerFor(RemoteInboundRTP.class);
	}

	private static ObjectReader getRemoteInboundRTPObjectReader() {
		if (RemoteInboundRTPReader == null) instantiateRemoteInboundRTPMapper();
		return RemoteInboundRTPReader;
	}

	private static ObjectWriter getRemoteInboundRTPObjectWriter() {
		if (RemoteInboundRTPWriter == null) instantiateRemoteInboundRTPMapper();
		return RemoteInboundRTPWriter;
	}

	private static ObjectReader ObserveRTCCIceStatsReader;
	private static ObjectWriter ObserveRTCCIceStatsWriter;

	private static void instantiateObserveRTCCIceStatsMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		ObserveRTCCIceStatsReader = mapper.readerFor(ObserveRTCCIceStats.class);
		ObserveRTCCIceStatsWriter = mapper.writerFor(ObserveRTCCIceStats.class);
	}

	private static ObjectReader getObserveRTCCIceStatsObjectReader() {
		if (ObserveRTCCIceStatsReader == null) instantiateObserveRTCCIceStatsMapper();
		return ObserveRTCCIceStatsReader;
	}

	private static ObjectWriter getObserveRTCCIceStatsObjectWriter() {
		if (ObserveRTCCIceStatsWriter == null) instantiateObserveRTCCIceStatsMapper();
		return ObserveRTCCIceStatsWriter;
	}

	private static ObjectReader ObserveRTCStatsReader;
	private static ObjectWriter ObserveRTCStatsWriter;

	private static void instantiateObserveRTCStatsMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		ObserveRTCStatsReader = mapper.readerFor(ObserveRTCStats.class);
		ObserveRTCStatsWriter = mapper.writerFor(ObserveRTCStats.class);
	}

	private static ObjectReader getObserveRTCStatsObjectReader() {
		if (ObserveRTCStatsReader == null) instantiateObserveRTCStatsMapper();
		return ObserveRTCStatsReader;
	}

	private static ObjectWriter getObserveRTCStatsObjectWriter() {
		if (ObserveRTCStatsWriter == null) instantiateObserveRTCStatsMapper();
		return ObserveRTCStatsWriter;
	}

	private static ObjectReader PeerConnectionSampleReader;
	private static ObjectWriter PeerConnectionSampleWriter;

	private static void instantiatePeerConnectionSampleMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		SimpleModule module = new SimpleModule();
		module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<OffsetDateTime>() {
			@Override
			public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
				String value = jsonParser.getText();
				return Converter.parseDateTimeString(value);
			}
		});
		mapper.registerModule(module);
		PeerConnectionSampleReader = mapper.readerFor(PeerConnectionSample.class);
		PeerConnectionSampleWriter = mapper.writerFor(PeerConnectionSample.class);
	}

	private static ObjectReader getPeerConnectionSampleObjectReader() {
		if (PeerConnectionSampleReader == null) instantiatePeerConnectionSampleMapper();
		return PeerConnectionSampleReader;
	}

	private static ObjectWriter getPeerConnectionSampleObjectWriter() {
		if (PeerConnectionSampleWriter == null) instantiatePeerConnectionSampleMapper();
		return PeerConnectionSampleWriter;
	}
}
