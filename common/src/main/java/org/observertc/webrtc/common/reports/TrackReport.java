//package org.observertc.webrtc.common.reports;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
//import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//public class TrackReport extends Report {
//
//	public static TrackReport of(UUID observerUUID,
//								 UUID peerConnectionUUID,
//								 LocalDateTime timestamp,
//								 String trackID,
//								 Double audioLevel,
//								 Integer concealedSamples,
//								 Integer concealmentEvents,
//								 Boolean detached,
//								 Boolean ended,
//								 Integer frameHeight,
//								 Integer framesDecoded,
//								 Integer framesDropped,
//								 Integer framesReceived,
//								 Integer framesSent,
//								 Integer frameWidth,
//								 Integer hugeFramesSent,
//								 Integer insertedSamplesForDeceleration,
//								 Double jitterBufferDelay,
//								 Integer jitterBufferEmittedCount,
//								 MediaType mediaType,
//								 Boolean remoteSource,
//								 Integer removedSamplesForAcceleration,
//								 Integer silentConcealedSamples,
//								 Double totalAudioEnergy,
//								 Double totalSamplesDuration,
//								 Integer totalSamplesReceived,
//								 String mediaSourceID
//	) {
//		TrackReport result = new TrackReport();
//		result.observerUUID = observerUUID;
//		result.peerConnectionUUID = peerConnectionUUID;
//		result.timestamp = timestamp;
//		result.trackID = trackID;
//		result.audioLevel = audioLevel;
//		result.concealedSamples = concealedSamples;
//		result.concealmentEvents = concealmentEvents;
//		result.detached = detached;
//		result.ended = ended;
//		result.frameHeight = frameHeight;
//		result.framesDecoded = framesDecoded;
//		result.framesDropped = framesDropped;
//		result.framesReceived = framesReceived;
//		result.framesSent = framesSent;
//		result.frameWidth = frameWidth;
//		result.hugeFramesSent = hugeFramesSent;
//		result.insertedSamplesForDeceleration = insertedSamplesForDeceleration;
//		result.jitterBufferDelay = jitterBufferDelay;
//		result.jitterBufferEmittedCount = jitterBufferEmittedCount;
//		result.mediaType = mediaType;
//		result.remoteSource = remoteSource;
//		result.removedSamplesForAcceleration = removedSamplesForAcceleration;
//		result.silentConcealedSamples = silentConcealedSamples;
//		result.totalAudioEnergy = totalAudioEnergy;
//		result.totalSamplesDuration = totalSamplesDuration;
//		result.totalSamplesReceived = totalSamplesReceived;
//		result.mediaSourceID = mediaSourceID;
//		return result;
//	}
//
//	public UUID observerUUID;
//
//	public UUID peerConnectionUUID;
//
//	@JsonSerialize(using = LocalDateTimeSerializer.class)
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	public LocalDateTime timestamp;
//
//	public String trackID;
//	public Double audioLevel;
//	public Integer concealedSamples;
//	public Integer concealmentEvents;
//	public Boolean detached;
//	public Boolean ended;
//	public Integer frameHeight;
//	public Integer framesDecoded;
//	public Integer framesDropped;
//	public Integer framesReceived;
//	public Integer framesSent;
//	public Integer frameWidth;
//	public Integer hugeFramesSent;
//	public Integer insertedSamplesForDeceleration;
//	public Double jitterBufferDelay;
//	public Integer jitterBufferEmittedCount;
//	public MediaType mediaType;
//	public Boolean remoteSource;
//	public Integer removedSamplesForAcceleration;
//	public Integer silentConcealedSamples;
//	public Double totalAudioEnergy;
//	public Double totalSamplesDuration;
//	public Integer totalSamplesReceived;
//	public String mediaSourceID;
//
//	@JsonCreator
//	public TrackReport() {
//		super(ReportType.TRACK_REPORT);
//	}
//}
