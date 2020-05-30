// Converter.java

// To use this code, add the following Maven dependency to your project:
//
//
//     com.fasterxml.jackson.core : jackson-databind : 2.9.0
//
// Import this package:
//
//     import io.quicktype.Converter;
//
// Then you can deserialize a JSON string with
//
//     MediaSource data = Converter.MediaSourceFromJsonString(jsonString);
//     CandidatePair data = Converter.CandidatePairFromJsonString(jsonString);
//     RemoteCandidate data = Converter.RemoteCandidateFromJsonString(jsonString);
//     LocalCandidate data = Converter.LocalCandidateFromJsonString(jsonString);
//     Track data = Converter.TrackFromJsonString(jsonString);
//     OutboundRTP data = Converter.OutboundRTPFromJsonString(jsonString);
//     InboundRTP data = Converter.InboundRTPFromJsonString(jsonString);
//     RemoteInboundRTP data = Converter.RemoteInboundRTPFromJsonString(jsonString);
//     StatsPayload data = Converter.StatsPayloadFromJsonString(jsonString);

package io.quicktype;

import java.util.*;
import java.io.IOException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Converter {
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

	public static StatsPayload StatsPayloadFromJsonString(String json) throws IOException {
		return getStatsPayloadObjectReader().readValue(json);
	}

	public static String StatsPayloadToJsonString(StatsPayload obj) throws JsonProcessingException {
		return getStatsPayloadObjectWriter().writeValueAsString(obj);
	}

	private static ObjectReader MediaSourceReader;
	private static ObjectWriter MediaSourceWriter;

	private static void instantiateMediaSourceMapper() {
		ObjectMapper mapper = new ObjectMapper();
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

	private static ObjectReader TrackReader;
	private static ObjectWriter TrackWriter;

	private static void instantiateTrackMapper() {
		ObjectMapper mapper = new ObjectMapper();
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

	private static ObjectReader StatsPayloadReader;
	private static ObjectWriter StatsPayloadWriter;

	private static void instantiateStatsPayloadMapper() {
		ObjectMapper mapper = new ObjectMapper();
		StatsPayloadReader = mapper.readerFor(StatsPayload.class);
		StatsPayloadWriter = mapper.writerFor(StatsPayload.class);
	}

	private static ObjectReader getStatsPayloadObjectReader() {
		if (StatsPayloadReader == null) instantiateStatsPayloadMapper();
		return StatsPayloadReader;
	}

	private static ObjectWriter getStatsPayloadObjectWriter() {
		if (StatsPayloadWriter == null) instantiateStatsPayloadMapper();
		return StatsPayloadWriter;
	}
}

// MediaSource.java

package io.quicktype;

		import java.util.*;
		import com.fasterxml.jackson.annotation.*;

public class MediaSource {
	private double audioLevel;
	private double framesPerSecond;
	private double height;
	private String id;
	private Kind kind;
	private double timestamp;
	private double totalAudioEnergy;
	private double totalSamplesDuration;
	private String trackIdentifier;
	private MediaSourceType type;
	private double width;

	@JsonProperty("audioLevel")
	public double getAudioLevel() { return audioLevel; }
	@JsonProperty("audioLevel")
	public void setAudioLevel(double value) { this.audioLevel = value; }

	@JsonProperty("framesPerSecond")
	public double getFramesPerSecond() { return framesPerSecond; }
	@JsonProperty("framesPerSecond")
	public void setFramesPerSecond(double value) { this.framesPerSecond = value; }

	@JsonProperty("height")
	public double getHeight() { return height; }
	@JsonProperty("height")
	public void setHeight(double value) { this.height = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("totalAudioEnergy")
	public double getTotalAudioEnergy() { return totalAudioEnergy; }
	@JsonProperty("totalAudioEnergy")
	public void setTotalAudioEnergy(double value) { this.totalAudioEnergy = value; }

	@JsonProperty("totalSamplesDuration")
	public double getTotalSamplesDuration() { return totalSamplesDuration; }
	@JsonProperty("totalSamplesDuration")
	public void setTotalSamplesDuration(double value) { this.totalSamplesDuration = value; }

	@JsonProperty("trackIdentifier")
	public String getTrackIdentifier() { return trackIdentifier; }
	@JsonProperty("trackIdentifier")
	public void setTrackIdentifier(String value) { this.trackIdentifier = value; }

	@JsonProperty("type")
	public MediaSourceType getType() { return type; }
	@JsonProperty("type")
	public void setType(MediaSourceType value) { this.type = value; }

	@JsonProperty("width")
	public double getWidth() { return width; }
	@JsonProperty("width")
	public void setWidth(double value) { this.width = value; }
}

// Kind.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum Kind {
	AUDIO, VIDEO;

	@JsonValue
	public String toValue() {
		switch (this) {
			case AUDIO: return "audio";
			case VIDEO: return "video";
		}
		return null;
	}

	@JsonCreator
	public static Kind forValue(String value) throws IOException {
		if (value.equals("audio")) return AUDIO;
		if (value.equals("video")) return VIDEO;
		throw new IOException("Cannot deserialize Kind");
	}
}

// MediaSourceType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum MediaSourceType {
	MEDIA_SOURCE;

	@JsonValue
	public String toValue() {
		switch (this) {
			case MEDIA_SOURCE: return "media-source";
		}
		return null;
	}

	@JsonCreator
	public static MediaSourceType forValue(String value) throws IOException {
		if (value.equals("media-source")) return MEDIA_SOURCE;
		throw new IOException("Cannot deserialize MediaSourceType");
	}
}

// CandidatePair.java

package io.quicktype;

		import java.util.*;
		import com.fasterxml.jackson.annotation.*;

public class CandidatePair {
	private double availableOutgoingBitrate;
	private double bytesReceived;
	private double bytesSent;
	private double consentRequestsSent;
	private double currentRoundTripTime;
	private String id;
	private String localCandidateID;
	private boolean nominated;
	private double priority;
	private String remoteCandidateID;
	private double requestsReceived;
	private double requestsSent;
	private double responsesReceived;
	private double responsesSent;
	private State state;
	private double timestamp;
	private double totalRoundTripTime;
	private String transportID;
	private CandidatePairType type;
	private boolean writable;

	@JsonProperty("availableOutgoingBitrate")
	public double getAvailableOutgoingBitrate() { return availableOutgoingBitrate; }
	@JsonProperty("availableOutgoingBitrate")
	public void setAvailableOutgoingBitrate(double value) { this.availableOutgoingBitrate = value; }

	@JsonProperty("bytesReceived")
	public double getBytesReceived() { return bytesReceived; }
	@JsonProperty("bytesReceived")
	public void setBytesReceived(double value) { this.bytesReceived = value; }

	@JsonProperty("bytesSent")
	public double getBytesSent() { return bytesSent; }
	@JsonProperty("bytesSent")
	public void setBytesSent(double value) { this.bytesSent = value; }

	@JsonProperty("consentRequestsSent")
	public double getConsentRequestsSent() { return consentRequestsSent; }
	@JsonProperty("consentRequestsSent")
	public void setConsentRequestsSent(double value) { this.consentRequestsSent = value; }

	@JsonProperty("currentRoundTripTime")
	public double getCurrentRoundTripTime() { return currentRoundTripTime; }
	@JsonProperty("currentRoundTripTime")
	public void setCurrentRoundTripTime(double value) { this.currentRoundTripTime = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("localCandidateId")
	public String getLocalCandidateID() { return localCandidateID; }
	@JsonProperty("localCandidateId")
	public void setLocalCandidateID(String value) { this.localCandidateID = value; }

	@JsonProperty("nominated")
	public boolean getNominated() { return nominated; }
	@JsonProperty("nominated")
	public void setNominated(boolean value) { this.nominated = value; }

	@JsonProperty("priority")
	public double getPriority() { return priority; }
	@JsonProperty("priority")
	public void setPriority(double value) { this.priority = value; }

	@JsonProperty("remoteCandidateId")
	public String getRemoteCandidateID() { return remoteCandidateID; }
	@JsonProperty("remoteCandidateId")
	public void setRemoteCandidateID(String value) { this.remoteCandidateID = value; }

	@JsonProperty("requestsReceived")
	public double getRequestsReceived() { return requestsReceived; }
	@JsonProperty("requestsReceived")
	public void setRequestsReceived(double value) { this.requestsReceived = value; }

	@JsonProperty("requestsSent")
	public double getRequestsSent() { return requestsSent; }
	@JsonProperty("requestsSent")
	public void setRequestsSent(double value) { this.requestsSent = value; }

	@JsonProperty("responsesReceived")
	public double getResponsesReceived() { return responsesReceived; }
	@JsonProperty("responsesReceived")
	public void setResponsesReceived(double value) { this.responsesReceived = value; }

	@JsonProperty("responsesSent")
	public double getResponsesSent() { return responsesSent; }
	@JsonProperty("responsesSent")
	public void setResponsesSent(double value) { this.responsesSent = value; }

	@JsonProperty("state")
	public State getState() { return state; }
	@JsonProperty("state")
	public void setState(State value) { this.state = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("totalRoundTripTime")
	public double getTotalRoundTripTime() { return totalRoundTripTime; }
	@JsonProperty("totalRoundTripTime")
	public void setTotalRoundTripTime(double value) { this.totalRoundTripTime = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("type")
	public CandidatePairType getType() { return type; }
	@JsonProperty("type")
	public void setType(CandidatePairType value) { this.type = value; }

	@JsonProperty("writable")
	public boolean getWritable() { return writable; }
	@JsonProperty("writable")
	public void setWritable(boolean value) { this.writable = value; }
}

// State.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum State {
	FAILED, FROZEN, IN_PROGRESS, SUCCEEDED, WAITING;

	@JsonValue
	public String toValue() {
		switch (this) {
			case FAILED: return "failed";
			case FROZEN: return "frozen";
			case IN_PROGRESS: return "in-progress";
			case SUCCEEDED: return "succeeded";
			case WAITING: return "waiting";
		}
		return null;
	}

	@JsonCreator
	public static State forValue(String value) throws IOException {
		if (value.equals("failed")) return FAILED;
		if (value.equals("frozen")) return FROZEN;
		if (value.equals("in-progress")) return IN_PROGRESS;
		if (value.equals("succeeded")) return SUCCEEDED;
		if (value.equals("waiting")) return WAITING;
		throw new IOException("Cannot deserialize State");
	}
}

// CandidatePairType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum CandidatePairType {
	CANDIDATE_PAIR;

	@JsonValue
	public String toValue() {
		switch (this) {
			case CANDIDATE_PAIR: return "candidate-pair";
		}
		return null;
	}

	@JsonCreator
	public static CandidatePairType forValue(String value) throws IOException {
		if (value.equals("candidate-pair")) return CANDIDATE_PAIR;
		throw new IOException("Cannot deserialize CandidatePairType");
	}
}

// RemoteCandidate.java

package io.quicktype;

		import java.util.*;
		import com.fasterxml.jackson.annotation.*;

public class RemoteCandidate {
	private CandidateType candidateType;
	private boolean deleted;
	private String id;
	private String ip;
	private boolean isRemote;
	private double port;
	private double priority;
	private Protocol protocol;
	private double timestamp;
	private TransportID transportID;
	private RemoteCandidateType type;

	@JsonProperty("candidateType")
	public CandidateType getCandidateType() { return candidateType; }
	@JsonProperty("candidateType")
	public void setCandidateType(CandidateType value) { this.candidateType = value; }

	@JsonProperty("deleted")
	public boolean getDeleted() { return deleted; }
	@JsonProperty("deleted")
	public void setDeleted(boolean value) { this.deleted = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("ip")
	public String getIP() { return ip; }
	@JsonProperty("ip")
	public void setIP(String value) { this.ip = value; }

	@JsonProperty("isRemote")
	public boolean getIsRemote() { return isRemote; }
	@JsonProperty("isRemote")
	public void setIsRemote(boolean value) { this.isRemote = value; }

	@JsonProperty("port")
	public double getPort() { return port; }
	@JsonProperty("port")
	public void setPort(double value) { this.port = value; }

	@JsonProperty("priority")
	public double getPriority() { return priority; }
	@JsonProperty("priority")
	public void setPriority(double value) { this.priority = value; }

	@JsonProperty("protocol")
	public Protocol getProtocol() { return protocol; }
	@JsonProperty("protocol")
	public void setProtocol(Protocol value) { this.protocol = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("transportId")
	public TransportID getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(TransportID value) { this.transportID = value; }

	@JsonProperty("type")
	public RemoteCandidateType getType() { return type; }
	@JsonProperty("type")
	public void setType(RemoteCandidateType value) { this.type = value; }
}

// CandidateType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum CandidateType {
	HOST, PRFLX, RELAY, SRFLX;

	@JsonValue
	public String toValue() {
		switch (this) {
			case HOST: return "host";
			case PRFLX: return "prflx";
			case RELAY: return "relay";
			case SRFLX: return "srflx";
		}
		return null;
	}

	@JsonCreator
	public static CandidateType forValue(String value) throws IOException {
		if (value.equals("host")) return HOST;
		if (value.equals("prflx")) return PRFLX;
		if (value.equals("relay")) return RELAY;
		if (value.equals("srflx")) return SRFLX;
		throw new IOException("Cannot deserialize CandidateType");
	}
}

// Protocol.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum Protocol {
	TCP, UDP;

	@JsonValue
	public String toValue() {
		switch (this) {
			case TCP: return "tcp";
			case UDP: return "udp";
		}
		return null;
	}

	@JsonCreator
	public static Protocol forValue(String value) throws IOException {
		if (value.equals("tcp")) return TCP;
		if (value.equals("udp")) return UDP;
		throw new IOException("Cannot deserialize Protocol");
	}
}

// TransportID.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum TransportID {
	STRING;

	@JsonValue
	public String toValue() {
		switch (this) {
			case STRING: return "string";
		}
		return null;
	}

	@JsonCreator
	public static TransportID forValue(String value) throws IOException {
		if (value.equals("string")) return STRING;
		throw new IOException("Cannot deserialize TransportID");
	}
}

// RemoteCandidateType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum RemoteCandidateType {
	REMOTE_CANDIDATE;

	@JsonValue
	public String toValue() {
		switch (this) {
			case REMOTE_CANDIDATE: return "remote-candidate";
		}
		return null;
	}

	@JsonCreator
	public static RemoteCandidateType forValue(String value) throws IOException {
		if (value.equals("remote-candidate")) return REMOTE_CANDIDATE;
		throw new IOException("Cannot deserialize RemoteCandidateType");
	}
}

// LocalCandidate.java

package io.quicktype;

		import java.util.*;
		import com.fasterxml.jackson.annotation.*;

public class LocalCandidate {
	private CandidateType candidateType;
	private boolean deleted;
	private String id;
	private String ip;
	private boolean isRemote;
	private NetworkType networkType;
	private double port;
	private double priority;
	private Protocol protocol;
	private double timestamp;
	private String transportID;
	private LocalCandidateType type;

	@JsonProperty("candidateType")
	public CandidateType getCandidateType() { return candidateType; }
	@JsonProperty("candidateType")
	public void setCandidateType(CandidateType value) { this.candidateType = value; }

	@JsonProperty("deleted")
	public boolean getDeleted() { return deleted; }
	@JsonProperty("deleted")
	public void setDeleted(boolean value) { this.deleted = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("ip")
	public String getIP() { return ip; }
	@JsonProperty("ip")
	public void setIP(String value) { this.ip = value; }

	@JsonProperty("isRemote")
	public boolean getIsRemote() { return isRemote; }
	@JsonProperty("isRemote")
	public void setIsRemote(boolean value) { this.isRemote = value; }

	@JsonProperty("networkType")
	public NetworkType getNetworkType() { return networkType; }
	@JsonProperty("networkType")
	public void setNetworkType(NetworkType value) { this.networkType = value; }

	@JsonProperty("port")
	public double getPort() { return port; }
	@JsonProperty("port")
	public void setPort(double value) { this.port = value; }

	@JsonProperty("priority")
	public double getPriority() { return priority; }
	@JsonProperty("priority")
	public void setPriority(double value) { this.priority = value; }

	@JsonProperty("protocol")
	public Protocol getProtocol() { return protocol; }
	@JsonProperty("protocol")
	public void setProtocol(Protocol value) { this.protocol = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("type")
	public LocalCandidateType getType() { return type; }
	@JsonProperty("type")
	public void setType(LocalCandidateType value) { this.type = value; }
}

// NetworkType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum NetworkType {
	BLUETOOTH, CELLULAR, ETHERNET, UNKNOWN, VPN, WIFI, WIMAX;

	@JsonValue
	public String toValue() {
		switch (this) {
			case BLUETOOTH: return "bluetooth";
			case CELLULAR: return "cellular";
			case ETHERNET: return "ethernet";
			case UNKNOWN: return "unknown";
			case VPN: return "vpn";
			case WIFI: return "wifi";
			case WIMAX: return "wimax";
		}
		return null;
	}

	@JsonCreator
	public static NetworkType forValue(String value) throws IOException {
		if (value.equals("bluetooth")) return BLUETOOTH;
		if (value.equals("cellular")) return CELLULAR;
		if (value.equals("ethernet")) return ETHERNET;
		if (value.equals("unknown")) return UNKNOWN;
		if (value.equals("vpn")) return VPN;
		if (value.equals("wifi")) return WIFI;
		if (value.equals("wimax")) return WIMAX;
		throw new IOException("Cannot deserialize NetworkType");
	}
}

// LocalCandidateType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum LocalCandidateType {
	LOCAL_CANDIDATE;

	@JsonValue
	public String toValue() {
		switch (this) {
			case LOCAL_CANDIDATE: return "local-candidate";
		}
		return null;
	}

	@JsonCreator
	public static LocalCandidateType forValue(String value) throws IOException {
		if (value.equals("local-candidate")) return LOCAL_CANDIDATE;
		throw new IOException("Cannot deserialize LocalCandidateType");
	}
}

// Track.java

package io.quicktype;

		import java.util.*;
		import com.fasterxml.jackson.annotation.*;

public class Track {
	private double audioLevel;
	private double concealedSamples;
	private double concealmentEvents;
	private boolean detached;
	private boolean ended;
	private double frameHeight;
	private double framesDecoded;
	private double framesDropped;
	private double framesReceived;
	private double framesSent;
	private double frameWidth;
	private double hugeFramesSent;
	private String id;
	private double insertedSamplesForDeceleration;
	private double jitterBufferDelay;
	private double jitterBufferEmittedCount;
	private Kind kind;
	private String mediaSourceID;
	private boolean remoteSource;
	private double removedSamplesForAcceleration;
	private double silentConcealedSamples;
	private String timestamp;
	private double totalAudioEnergy;
	private double totalSamplesDuration;
	private double totalSamplesReceived;
	private String trackIdentifier;
	private TrackType type;

	@JsonProperty("audioLevel")
	public double getAudioLevel() { return audioLevel; }
	@JsonProperty("audioLevel")
	public void setAudioLevel(double value) { this.audioLevel = value; }

	@JsonProperty("concealedSamples")
	public double getConcealedSamples() { return concealedSamples; }
	@JsonProperty("concealedSamples")
	public void setConcealedSamples(double value) { this.concealedSamples = value; }

	@JsonProperty("concealmentEvents")
	public double getConcealmentEvents() { return concealmentEvents; }
	@JsonProperty("concealmentEvents")
	public void setConcealmentEvents(double value) { this.concealmentEvents = value; }

	@JsonProperty("detached")
	public boolean getDetached() { return detached; }
	@JsonProperty("detached")
	public void setDetached(boolean value) { this.detached = value; }

	@JsonProperty("ended")
	public boolean getEnded() { return ended; }
	@JsonProperty("ended")
	public void setEnded(boolean value) { this.ended = value; }

	@JsonProperty("frameHeight")
	public double getFrameHeight() { return frameHeight; }
	@JsonProperty("frameHeight")
	public void setFrameHeight(double value) { this.frameHeight = value; }

	@JsonProperty("framesDecoded")
	public double getFramesDecoded() { return framesDecoded; }
	@JsonProperty("framesDecoded")
	public void setFramesDecoded(double value) { this.framesDecoded = value; }

	@JsonProperty("framesDropped")
	public double getFramesDropped() { return framesDropped; }
	@JsonProperty("framesDropped")
	public void setFramesDropped(double value) { this.framesDropped = value; }

	@JsonProperty("framesReceived")
	public double getFramesReceived() { return framesReceived; }
	@JsonProperty("framesReceived")
	public void setFramesReceived(double value) { this.framesReceived = value; }

	@JsonProperty("framesSent")
	public double getFramesSent() { return framesSent; }
	@JsonProperty("framesSent")
	public void setFramesSent(double value) { this.framesSent = value; }

	@JsonProperty("frameWidth")
	public double getFrameWidth() { return frameWidth; }
	@JsonProperty("frameWidth")
	public void setFrameWidth(double value) { this.frameWidth = value; }

	@JsonProperty("hugeFramesSent")
	public double getHugeFramesSent() { return hugeFramesSent; }
	@JsonProperty("hugeFramesSent")
	public void setHugeFramesSent(double value) { this.hugeFramesSent = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("insertedSamplesForDeceleration")
	public double getInsertedSamplesForDeceleration() { return insertedSamplesForDeceleration; }
	@JsonProperty("insertedSamplesForDeceleration")
	public void setInsertedSamplesForDeceleration(double value) { this.insertedSamplesForDeceleration = value; }

	@JsonProperty("jitterBufferDelay")
	public double getJitterBufferDelay() { return jitterBufferDelay; }
	@JsonProperty("jitterBufferDelay")
	public void setJitterBufferDelay(double value) { this.jitterBufferDelay = value; }

	@JsonProperty("jitterBufferEmittedCount")
	public double getJitterBufferEmittedCount() { return jitterBufferEmittedCount; }
	@JsonProperty("jitterBufferEmittedCount")
	public void setJitterBufferEmittedCount(double value) { this.jitterBufferEmittedCount = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("mediaSourceId")
	public String getMediaSourceID() { return mediaSourceID; }
	@JsonProperty("mediaSourceId")
	public void setMediaSourceID(String value) { this.mediaSourceID = value; }

	@JsonProperty("remoteSource")
	public boolean getRemoteSource() { return remoteSource; }
	@JsonProperty("remoteSource")
	public void setRemoteSource(boolean value) { this.remoteSource = value; }

	@JsonProperty("removedSamplesForAcceleration")
	public double getRemovedSamplesForAcceleration() { return removedSamplesForAcceleration; }
	@JsonProperty("removedSamplesForAcceleration")
	public void setRemovedSamplesForAcceleration(double value) { this.removedSamplesForAcceleration = value; }

	@JsonProperty("silentConcealedSamples")
	public double getSilentConcealedSamples() { return silentConcealedSamples; }
	@JsonProperty("silentConcealedSamples")
	public void setSilentConcealedSamples(double value) { this.silentConcealedSamples = value; }

	@JsonProperty("timestamp")
	public String getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(String value) { this.timestamp = value; }

	@JsonProperty("totalAudioEnergy")
	public double getTotalAudioEnergy() { return totalAudioEnergy; }
	@JsonProperty("totalAudioEnergy")
	public void setTotalAudioEnergy(double value) { this.totalAudioEnergy = value; }

	@JsonProperty("totalSamplesDuration")
	public double getTotalSamplesDuration() { return totalSamplesDuration; }
	@JsonProperty("totalSamplesDuration")
	public void setTotalSamplesDuration(double value) { this.totalSamplesDuration = value; }

	@JsonProperty("totalSamplesReceived")
	public double getTotalSamplesReceived() { return totalSamplesReceived; }
	@JsonProperty("totalSamplesReceived")
	public void setTotalSamplesReceived(double value) { this.totalSamplesReceived = value; }

	@JsonProperty("trackIdentifier")
	public String getTrackIdentifier() { return trackIdentifier; }
	@JsonProperty("trackIdentifier")
	public void setTrackIdentifier(String value) { this.trackIdentifier = value; }

	@JsonProperty("type")
	public TrackType getType() { return type; }
	@JsonProperty("type")
	public void setType(TrackType value) { this.type = value; }
}

// TrackType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum TrackType {
	TRACK;

	@JsonValue
	public String toValue() {
		switch (this) {
			case TRACK: return "track";
		}
		return null;
	}

	@JsonCreator
	public static TrackType forValue(String value) throws IOException {
		if (value.equals("track")) return TRACK;
		throw new IOException("Cannot deserialize TrackType");
	}
}

// OutboundRTP.java

package io.quicktype;

		import java.util.*;
		import com.fasterxml.jackson.annotation.*;

public class OutboundRTP {
	private double bytesSent;
	private String codecID;
	private String encoderImplementation;
	private double firCount;
	private double framesEncoded;
	private double headerBytesSent;
	private String id;
	private boolean isRemote;
	private double keyFramesEncoded;
	private Kind kind;
	private String mediaSourceID;
	private Kind mediaType;
	private double nackCount;
	private double packetsSent;
	private double pliCount;
	private double qpSum;
	private QualityLimitationReason qualityLimitationReason;
	private double qualityLimitationResolutionChanges;
	private String remoteID;
	private double retransmittedBytesSent;
	private double retransmittedPacketsSent;
	private double ssrc;
	private double timestamp;
	private double totalEncodedBytesTarget;
	private double totalEncodeTime;
	private double totalPacketSendDelay;
	private double trackID;
	private String transportID;
	private OutboundRTPType type;

	@JsonProperty("bytesSent")
	public double getBytesSent() { return bytesSent; }
	@JsonProperty("bytesSent")
	public void setBytesSent(double value) { this.bytesSent = value; }

	@JsonProperty("codecId")
	public String getCodecID() { return codecID; }
	@JsonProperty("codecId")
	public void setCodecID(String value) { this.codecID = value; }

	@JsonProperty("encoderImplementation")
	public String getEncoderImplementation() { return encoderImplementation; }
	@JsonProperty("encoderImplementation")
	public void setEncoderImplementation(String value) { this.encoderImplementation = value; }

	@JsonProperty("firCount")
	public double getFirCount() { return firCount; }
	@JsonProperty("firCount")
	public void setFirCount(double value) { this.firCount = value; }

	@JsonProperty("framesEncoded")
	public double getFramesEncoded() { return framesEncoded; }
	@JsonProperty("framesEncoded")
	public void setFramesEncoded(double value) { this.framesEncoded = value; }

	@JsonProperty("headerBytesSent")
	public double getHeaderBytesSent() { return headerBytesSent; }
	@JsonProperty("headerBytesSent")
	public void setHeaderBytesSent(double value) { this.headerBytesSent = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("isRemote")
	public boolean getIsRemote() { return isRemote; }
	@JsonProperty("isRemote")
	public void setIsRemote(boolean value) { this.isRemote = value; }

	@JsonProperty("keyFramesEncoded")
	public double getKeyFramesEncoded() { return keyFramesEncoded; }
	@JsonProperty("keyFramesEncoded")
	public void setKeyFramesEncoded(double value) { this.keyFramesEncoded = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("mediaSourceId")
	public String getMediaSourceID() { return mediaSourceID; }
	@JsonProperty("mediaSourceId")
	public void setMediaSourceID(String value) { this.mediaSourceID = value; }

	@JsonProperty("mediaType")
	public Kind getMediaType() { return mediaType; }
	@JsonProperty("mediaType")
	public void setMediaType(Kind value) { this.mediaType = value; }

	@JsonProperty("nackCount")
	public double getNACKCount() { return nackCount; }
	@JsonProperty("nackCount")
	public void setNACKCount(double value) { this.nackCount = value; }

	@JsonProperty("packetsSent")
	public double getPacketsSent() { return packetsSent; }
	@JsonProperty("packetsSent")
	public void setPacketsSent(double value) { this.packetsSent = value; }

	@JsonProperty("pliCount")
	public double getPliCount() { return pliCount; }
	@JsonProperty("pliCount")
	public void setPliCount(double value) { this.pliCount = value; }

	@JsonProperty("qpSum")
	public double getQpSum() { return qpSum; }
	@JsonProperty("qpSum")
	public void setQpSum(double value) { this.qpSum = value; }

	@JsonProperty("qualityLimitationReason")
	public QualityLimitationReason getQualityLimitationReason() { return qualityLimitationReason; }
	@JsonProperty("qualityLimitationReason")
	public void setQualityLimitationReason(QualityLimitationReason value) { this.qualityLimitationReason = value; }

	@JsonProperty("qualityLimitationResolutionChanges")
	public double getQualityLimitationResolutionChanges() { return qualityLimitationResolutionChanges; }
	@JsonProperty("qualityLimitationResolutionChanges")
	public void setQualityLimitationResolutionChanges(double value) { this.qualityLimitationResolutionChanges = value; }

	@JsonProperty("remoteId")
	public String getRemoteID() { return remoteID; }
	@JsonProperty("remoteId")
	public void setRemoteID(String value) { this.remoteID = value; }

	@JsonProperty("retransmittedBytesSent")
	public double getRetransmittedBytesSent() { return retransmittedBytesSent; }
	@JsonProperty("retransmittedBytesSent")
	public void setRetransmittedBytesSent(double value) { this.retransmittedBytesSent = value; }

	@JsonProperty("retransmittedPacketsSent")
	public double getRetransmittedPacketsSent() { return retransmittedPacketsSent; }
	@JsonProperty("retransmittedPacketsSent")
	public void setRetransmittedPacketsSent(double value) { this.retransmittedPacketsSent = value; }

	@JsonProperty("ssrc")
	public double getSsrc() { return ssrc; }
	@JsonProperty("ssrc")
	public void setSsrc(double value) { this.ssrc = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("totalEncodedBytesTarget")
	public double getTotalEncodedBytesTarget() { return totalEncodedBytesTarget; }
	@JsonProperty("totalEncodedBytesTarget")
	public void setTotalEncodedBytesTarget(double value) { this.totalEncodedBytesTarget = value; }

	@JsonProperty("totalEncodeTime")
	public double getTotalEncodeTime() { return totalEncodeTime; }
	@JsonProperty("totalEncodeTime")
	public void setTotalEncodeTime(double value) { this.totalEncodeTime = value; }

	@JsonProperty("totalPacketSendDelay")
	public double getTotalPacketSendDelay() { return totalPacketSendDelay; }
	@JsonProperty("totalPacketSendDelay")
	public void setTotalPacketSendDelay(double value) { this.totalPacketSendDelay = value; }

	@JsonProperty("trackId")
	public double getTrackID() { return trackID; }
	@JsonProperty("trackId")
	public void setTrackID(double value) { this.trackID = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("type")
	public OutboundRTPType getType() { return type; }
	@JsonProperty("type")
	public void setType(OutboundRTPType value) { this.type = value; }
}

// QualityLimitationReason.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum QualityLimitationReason {
	BANDWIDTH, CPU, NONE, OTHER;

	@JsonValue
	public String toValue() {
		switch (this) {
			case BANDWIDTH: return "bandwidth";
			case CPU: return "cpu";
			case NONE: return "none";
			case OTHER: return "other";
		}
		return null;
	}

	@JsonCreator
	public static QualityLimitationReason forValue(String value) throws IOException {
		if (value.equals("bandwidth")) return BANDWIDTH;
		if (value.equals("cpu")) return CPU;
		if (value.equals("none")) return NONE;
		if (value.equals("other")) return OTHER;
		throw new IOException("Cannot deserialize QualityLimitationReason");
	}
}

// OutboundRTPType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum OutboundRTPType {
	OUTBOUND_RTP;

	@JsonValue
	public String toValue() {
		switch (this) {
			case OUTBOUND_RTP: return "outbound-rtp";
		}
		return null;
	}

	@JsonCreator
	public static OutboundRTPType forValue(String value) throws IOException {
		if (value.equals("outbound-rtp")) return OUTBOUND_RTP;
		throw new IOException("Cannot deserialize OutboundRTPType");
	}
}

// InboundRTP.java

package io.quicktype;

		import java.util.*;
		import com.fasterxml.jackson.annotation.*;

public class InboundRTP {
	private double bytesReceived;
	private String codecID;
	private String decoderImplementation;
	private double estimatedPlayoutTimestamp;
	private double fecPacketsDiscarded;
	private double fecPacketsReceived;
	private double firCount;
	private double framesDecoded;
	private double headerBytesReceived;
	private String id;
	private boolean isRemote;
	private double jitter;
	private double keyFramesDecoded;
	private Kind kind;
	private double lastPacketReceivedTimestamp;
	private Kind mediaType;
	private double nackCount;
	private double packetsLost;
	private double packetsReceived;
	private double pliCount;
	private double qpSum;
	private double ssrc;
	private double timestamp;
	private double totalDecodeTime;
	private double totalInterFrameDelay;
	private double totalSquaredInterFrameDelay;
	private String trackID;
	private String transportID;
	private InboundRTPType type;

	@JsonProperty("bytesReceived")
	public double getBytesReceived() { return bytesReceived; }
	@JsonProperty("bytesReceived")
	public void setBytesReceived(double value) { this.bytesReceived = value; }

	@JsonProperty("codecId")
	public String getCodecID() { return codecID; }
	@JsonProperty("codecId")
	public void setCodecID(String value) { this.codecID = value; }

	@JsonProperty("decoderImplementation")
	public String getDecoderImplementation() { return decoderImplementation; }
	@JsonProperty("decoderImplementation")
	public void setDecoderImplementation(String value) { this.decoderImplementation = value; }

	@JsonProperty("estimatedPlayoutTimestamp")
	public double getEstimatedPlayoutTimestamp() { return estimatedPlayoutTimestamp; }
	@JsonProperty("estimatedPlayoutTimestamp")
	public void setEstimatedPlayoutTimestamp(double value) { this.estimatedPlayoutTimestamp = value; }

	@JsonProperty("fecPacketsDiscarded")
	public double getFECPacketsDiscarded() { return fecPacketsDiscarded; }
	@JsonProperty("fecPacketsDiscarded")
	public void setFECPacketsDiscarded(double value) { this.fecPacketsDiscarded = value; }

	@JsonProperty("fecPacketsReceived")
	public double getFECPacketsReceived() { return fecPacketsReceived; }
	@JsonProperty("fecPacketsReceived")
	public void setFECPacketsReceived(double value) { this.fecPacketsReceived = value; }

	@JsonProperty("firCount")
	public double getFirCount() { return firCount; }
	@JsonProperty("firCount")
	public void setFirCount(double value) { this.firCount = value; }

	@JsonProperty("framesDecoded")
	public double getFramesDecoded() { return framesDecoded; }
	@JsonProperty("framesDecoded")
	public void setFramesDecoded(double value) { this.framesDecoded = value; }

	@JsonProperty("headerBytesReceived")
	public double getHeaderBytesReceived() { return headerBytesReceived; }
	@JsonProperty("headerBytesReceived")
	public void setHeaderBytesReceived(double value) { this.headerBytesReceived = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("isRemote")
	public boolean getIsRemote() { return isRemote; }
	@JsonProperty("isRemote")
	public void setIsRemote(boolean value) { this.isRemote = value; }

	@JsonProperty("jitter")
	public double getJitter() { return jitter; }
	@JsonProperty("jitter")
	public void setJitter(double value) { this.jitter = value; }

	@JsonProperty("keyFramesDecoded")
	public double getKeyFramesDecoded() { return keyFramesDecoded; }
	@JsonProperty("keyFramesDecoded")
	public void setKeyFramesDecoded(double value) { this.keyFramesDecoded = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("lastPacketReceivedTimestamp")
	public double getLastPacketReceivedTimestamp() { return lastPacketReceivedTimestamp; }
	@JsonProperty("lastPacketReceivedTimestamp")
	public void setLastPacketReceivedTimestamp(double value) { this.lastPacketReceivedTimestamp = value; }

	@JsonProperty("mediaType")
	public Kind getMediaType() { return mediaType; }
	@JsonProperty("mediaType")
	public void setMediaType(Kind value) { this.mediaType = value; }

	@JsonProperty("nackCount")
	public double getNACKCount() { return nackCount; }
	@JsonProperty("nackCount")
	public void setNACKCount(double value) { this.nackCount = value; }

	@JsonProperty("packetsLost")
	public double getPacketsLost() { return packetsLost; }
	@JsonProperty("packetsLost")
	public void setPacketsLost(double value) { this.packetsLost = value; }

	@JsonProperty("packetsReceived")
	public double getPacketsReceived() { return packetsReceived; }
	@JsonProperty("packetsReceived")
	public void setPacketsReceived(double value) { this.packetsReceived = value; }

	@JsonProperty("pliCount")
	public double getPliCount() { return pliCount; }
	@JsonProperty("pliCount")
	public void setPliCount(double value) { this.pliCount = value; }

	@JsonProperty("qpSum")
	public double getQpSum() { return qpSum; }
	@JsonProperty("qpSum")
	public void setQpSum(double value) { this.qpSum = value; }

	@JsonProperty("ssrc")
	public double getSsrc() { return ssrc; }
	@JsonProperty("ssrc")
	public void setSsrc(double value) { this.ssrc = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("totalDecodeTime")
	public double getTotalDecodeTime() { return totalDecodeTime; }
	@JsonProperty("totalDecodeTime")
	public void setTotalDecodeTime(double value) { this.totalDecodeTime = value; }

	@JsonProperty("totalInterFrameDelay")
	public double getTotalInterFrameDelay() { return totalInterFrameDelay; }
	@JsonProperty("totalInterFrameDelay")
	public void setTotalInterFrameDelay(double value) { this.totalInterFrameDelay = value; }

	@JsonProperty("totalSquaredInterFrameDelay")
	public double getTotalSquaredInterFrameDelay() { return totalSquaredInterFrameDelay; }
	@JsonProperty("totalSquaredInterFrameDelay")
	public void setTotalSquaredInterFrameDelay(double value) { this.totalSquaredInterFrameDelay = value; }

	@JsonProperty("trackId")
	public String getTrackID() { return trackID; }
	@JsonProperty("trackId")
	public void setTrackID(String value) { this.trackID = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("type")
	public InboundRTPType getType() { return type; }
	@JsonProperty("type")
	public void setType(InboundRTPType value) { this.type = value; }
}

// InboundRTPType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum InboundRTPType {
	INBOUND_RTP;

	@JsonValue
	public String toValue() {
		switch (this) {
			case INBOUND_RTP: return "inbound-rtp";
		}
		return null;
	}

	@JsonCreator
	public static InboundRTPType forValue(String value) throws IOException {
		if (value.equals("inbound-rtp")) return INBOUND_RTP;
		throw new IOException("Cannot deserialize InboundRTPType");
	}
}

// RemoteInboundRTP.java

package io.quicktype;

		import java.util.*;
		import com.fasterxml.jackson.annotation.*;

public class RemoteInboundRTP {
	private String codecID;
	private String id;
	private double jitter;
	private Kind kind;
	private String localID;
	private double packetsLost;
	private double roundTripTime;
	private double ssrc;
	private double timestamp;
	private String transportID;
	private RemoteInboundRTPType type;

	@JsonProperty("codecId")
	public String getCodecID() { return codecID; }
	@JsonProperty("codecId")
	public void setCodecID(String value) { this.codecID = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("jitter")
	public double getJitter() { return jitter; }
	@JsonProperty("jitter")
	public void setJitter(double value) { this.jitter = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("localId")
	public String getLocalID() { return localID; }
	@JsonProperty("localId")
	public void setLocalID(String value) { this.localID = value; }

	@JsonProperty("packetsLost")
	public double getPacketsLost() { return packetsLost; }
	@JsonProperty("packetsLost")
	public void setPacketsLost(double value) { this.packetsLost = value; }

	@JsonProperty("roundTripTime")
	public double getRoundTripTime() { return roundTripTime; }
	@JsonProperty("roundTripTime")
	public void setRoundTripTime(double value) { this.roundTripTime = value; }

	@JsonProperty("ssrc")
	public double getSsrc() { return ssrc; }
	@JsonProperty("ssrc")
	public void setSsrc(double value) { this.ssrc = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("type")
	public RemoteInboundRTPType getType() { return type; }
	@JsonProperty("type")
	public void setType(RemoteInboundRTPType value) { this.type = value; }
}

// RemoteInboundRTPType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum RemoteInboundRTPType {
	REMOTE_INBOUND_RTP;

	@JsonValue
	public String toValue() {
		switch (this) {
			case REMOTE_INBOUND_RTP: return "remote-inbound-rtp";
		}
		return null;
	}

	@JsonCreator
	public static RemoteInboundRTPType forValue(String value) throws IOException {
		if (value.equals("remote-inbound-rtp")) return REMOTE_INBOUND_RTP;
		throw new IOException("Cannot deserialize RemoteInboundRTPType");
	}
}

// StatsPayload.java

package io.quicktype;

		import java.util.*;
		import com.fasterxml.jackson.annotation.*;

public class StatsPayload {
	private String peerConnectionID;
	private ReceiverStatElement[] receiverStats;
	private SenderStatElement[] senderStats;

	@JsonProperty("peerConnectionId")
	public String getPeerConnectionID() { return peerConnectionID; }
	@JsonProperty("peerConnectionId")
	public void setPeerConnectionID(String value) { this.peerConnectionID = value; }

	@JsonProperty("receiverStats")
	public ReceiverStatElement[] getReceiverStats() { return receiverStats; }
	@JsonProperty("receiverStats")
	public void setReceiverStats(ReceiverStatElement[] value) { this.receiverStats = value; }

	@JsonProperty("senderStats")
	public SenderStatElement[] getSenderStats() { return senderStats; }
	@JsonProperty("senderStats")
	public void setSenderStats(SenderStatElement[] value) { this.senderStats = value; }
}

// ReceiverStatElement.java

package io.quicktype;

		import java.util.*;
		import com.fasterxml.jackson.annotation.*;

public class ReceiverStatElement {
	private Double availableOutgoingBitrate;
	private Double bytesReceived;
	private Double bytesSent;
	private Double consentRequestsSent;
	private Double currentRoundTripTime;
	private String id;
	private String localCandidateID;
	private Boolean nominated;
	private Double priority;
	private String remoteCandidateID;
	private Double requestsReceived;
	private Double requestsSent;
	private Double responsesReceived;
	private Double responsesSent;
	private State state;
	private Timestamp timestamp;
	private Double totalRoundTripTime;
	private String transportID;
	private ReceiverStatType type;
	private Boolean writable;
	private CandidateType candidateType;
	private Boolean deleted;
	private String ip;
	private Boolean isRemote;
	private Double port;
	private Protocol protocol;
	private NetworkType networkType;
	private Double audioLevel;
	private Double concealedSamples;
	private Double concealmentEvents;
	private Boolean detached;
	private Boolean ended;
	private Double frameHeight;
	private Double framesDecoded;
	private Double framesDropped;
	private Double framesReceived;
	private Double framesSent;
	private Double frameWidth;
	private Double hugeFramesSent;
	private Double insertedSamplesForDeceleration;
	private Double jitterBufferDelay;
	private Double jitterBufferEmittedCount;
	private Kind kind;
	private String mediaSourceID;
	private Boolean remoteSource;
	private Double removedSamplesForAcceleration;
	private Double silentConcealedSamples;
	private Double totalAudioEnergy;
	private Double totalSamplesDuration;
	private Double totalSamplesReceived;
	private String trackIdentifier;
	private String codecID;
	private String decoderImplementation;
	private Double estimatedPlayoutTimestamp;
	private Double fecPacketsDiscarded;
	private Double fecPacketsReceived;
	private Double firCount;
	private Double headerBytesReceived;
	private Double jitter;
	private Double keyFramesDecoded;
	private Double lastPacketReceivedTimestamp;
	private Kind mediaType;
	private Double nackCount;
	private Double packetsLost;
	private Double packetsReceived;
	private Double pliCount;
	private Double qpSum;
	private Double ssrc;
	private Double totalDecodeTime;
	private Double totalInterFrameDelay;
	private Double totalSquaredInterFrameDelay;
	private String trackID;

	@JsonProperty("availableOutgoingBitrate")
	public Double getAvailableOutgoingBitrate() { return availableOutgoingBitrate; }
	@JsonProperty("availableOutgoingBitrate")
	public void setAvailableOutgoingBitrate(Double value) { this.availableOutgoingBitrate = value; }

	@JsonProperty("bytesReceived")
	public Double getBytesReceived() { return bytesReceived; }
	@JsonProperty("bytesReceived")
	public void setBytesReceived(Double value) { this.bytesReceived = value; }

	@JsonProperty("bytesSent")
	public Double getBytesSent() { return bytesSent; }
	@JsonProperty("bytesSent")
	public void setBytesSent(Double value) { this.bytesSent = value; }

	@JsonProperty("consentRequestsSent")
	public Double getConsentRequestsSent() { return consentRequestsSent; }
	@JsonProperty("consentRequestsSent")
	public void setConsentRequestsSent(Double value) { this.consentRequestsSent = value; }

	@JsonProperty("currentRoundTripTime")
	public Double getCurrentRoundTripTime() { return currentRoundTripTime; }
	@JsonProperty("currentRoundTripTime")
	public void setCurrentRoundTripTime(Double value) { this.currentRoundTripTime = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("localCandidateId")
	public String getLocalCandidateID() { return localCandidateID; }
	@JsonProperty("localCandidateId")
	public void setLocalCandidateID(String value) { this.localCandidateID = value; }

	@JsonProperty("nominated")
	public Boolean getNominated() { return nominated; }
	@JsonProperty("nominated")
	public void setNominated(Boolean value) { this.nominated = value; }

	@JsonProperty("priority")
	public Double getPriority() { return priority; }
	@JsonProperty("priority")
	public void setPriority(Double value) { this.priority = value; }

	@JsonProperty("remoteCandidateId")
	public String getRemoteCandidateID() { return remoteCandidateID; }
	@JsonProperty("remoteCandidateId")
	public void setRemoteCandidateID(String value) { this.remoteCandidateID = value; }

	@JsonProperty("requestsReceived")
	public Double getRequestsReceived() { return requestsReceived; }
	@JsonProperty("requestsReceived")
	public void setRequestsReceived(Double value) { this.requestsReceived = value; }

	@JsonProperty("requestsSent")
	public Double getRequestsSent() { return requestsSent; }
	@JsonProperty("requestsSent")
	public void setRequestsSent(Double value) { this.requestsSent = value; }

	@JsonProperty("responsesReceived")
	public Double getResponsesReceived() { return responsesReceived; }
	@JsonProperty("responsesReceived")
	public void setResponsesReceived(Double value) { this.responsesReceived = value; }

	@JsonProperty("responsesSent")
	public Double getResponsesSent() { return responsesSent; }
	@JsonProperty("responsesSent")
	public void setResponsesSent(Double value) { this.responsesSent = value; }

	@JsonProperty("state")
	public State getState() { return state; }
	@JsonProperty("state")
	public void setState(State value) { this.state = value; }

	@JsonProperty("timestamp")
	public Timestamp getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(Timestamp value) { this.timestamp = value; }

	@JsonProperty("totalRoundTripTime")
	public Double getTotalRoundTripTime() { return totalRoundTripTime; }
	@JsonProperty("totalRoundTripTime")
	public void setTotalRoundTripTime(Double value) { this.totalRoundTripTime = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("type")
	public ReceiverStatType getType() { return type; }
	@JsonProperty("type")
	public void setType(ReceiverStatType value) { this.type = value; }

	@JsonProperty("writable")
	public Boolean getWritable() { return writable; }
	@JsonProperty("writable")
	public void setWritable(Boolean value) { this.writable = value; }

	@JsonProperty("candidateType")
	public CandidateType getCandidateType() { return candidateType; }
	@JsonProperty("candidateType")
	public void setCandidateType(CandidateType value) { this.candidateType = value; }

	@JsonProperty("deleted")
	public Boolean getDeleted() { return deleted; }
	@JsonProperty("deleted")
	public void setDeleted(Boolean value) { this.deleted = value; }

	@JsonProperty("ip")
	public String getIP() { return ip; }
	@JsonProperty("ip")
	public void setIP(String value) { this.ip = value; }

	@JsonProperty("isRemote")
	public Boolean getIsRemote() { return isRemote; }
	@JsonProperty("isRemote")
	public void setIsRemote(Boolean value) { this.isRemote = value; }

	@JsonProperty("port")
	public Double getPort() { return port; }
	@JsonProperty("port")
	public void setPort(Double value) { this.port = value; }

	@JsonProperty("protocol")
	public Protocol getProtocol() { return protocol; }
	@JsonProperty("protocol")
	public void setProtocol(Protocol value) { this.protocol = value; }

	@JsonProperty("networkType")
	public NetworkType getNetworkType() { return networkType; }
	@JsonProperty("networkType")
	public void setNetworkType(NetworkType value) { this.networkType = value; }

	@JsonProperty("audioLevel")
	public Double getAudioLevel() { return audioLevel; }
	@JsonProperty("audioLevel")
	public void setAudioLevel(Double value) { this.audioLevel = value; }

	@JsonProperty("concealedSamples")
	public Double getConcealedSamples() { return concealedSamples; }
	@JsonProperty("concealedSamples")
	public void setConcealedSamples(Double value) { this.concealedSamples = value; }

	@JsonProperty("concealmentEvents")
	public Double getConcealmentEvents() { return concealmentEvents; }
	@JsonProperty("concealmentEvents")
	public void setConcealmentEvents(Double value) { this.concealmentEvents = value; }

	@JsonProperty("detached")
	public Boolean getDetached() { return detached; }
	@JsonProperty("detached")
	public void setDetached(Boolean value) { this.detached = value; }

	@JsonProperty("ended")
	public Boolean getEnded() { return ended; }
	@JsonProperty("ended")
	public void setEnded(Boolean value) { this.ended = value; }

	@JsonProperty("frameHeight")
	public Double getFrameHeight() { return frameHeight; }
	@JsonProperty("frameHeight")
	public void setFrameHeight(Double value) { this.frameHeight = value; }

	@JsonProperty("framesDecoded")
	public Double getFramesDecoded() { return framesDecoded; }
	@JsonProperty("framesDecoded")
	public void setFramesDecoded(Double value) { this.framesDecoded = value; }

	@JsonProperty("framesDropped")
	public Double getFramesDropped() { return framesDropped; }
	@JsonProperty("framesDropped")
	public void setFramesDropped(Double value) { this.framesDropped = value; }

	@JsonProperty("framesReceived")
	public Double getFramesReceived() { return framesReceived; }
	@JsonProperty("framesReceived")
	public void setFramesReceived(Double value) { this.framesReceived = value; }

	@JsonProperty("framesSent")
	public Double getFramesSent() { return framesSent; }
	@JsonProperty("framesSent")
	public void setFramesSent(Double value) { this.framesSent = value; }

	@JsonProperty("frameWidth")
	public Double getFrameWidth() { return frameWidth; }
	@JsonProperty("frameWidth")
	public void setFrameWidth(Double value) { this.frameWidth = value; }

	@JsonProperty("hugeFramesSent")
	public Double getHugeFramesSent() { return hugeFramesSent; }
	@JsonProperty("hugeFramesSent")
	public void setHugeFramesSent(Double value) { this.hugeFramesSent = value; }

	@JsonProperty("insertedSamplesForDeceleration")
	public Double getInsertedSamplesForDeceleration() { return insertedSamplesForDeceleration; }
	@JsonProperty("insertedSamplesForDeceleration")
	public void setInsertedSamplesForDeceleration(Double value) { this.insertedSamplesForDeceleration = value; }

	@JsonProperty("jitterBufferDelay")
	public Double getJitterBufferDelay() { return jitterBufferDelay; }
	@JsonProperty("jitterBufferDelay")
	public void setJitterBufferDelay(Double value) { this.jitterBufferDelay = value; }

	@JsonProperty("jitterBufferEmittedCount")
	public Double getJitterBufferEmittedCount() { return jitterBufferEmittedCount; }
	@JsonProperty("jitterBufferEmittedCount")
	public void setJitterBufferEmittedCount(Double value) { this.jitterBufferEmittedCount = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("mediaSourceId")
	public String getMediaSourceID() { return mediaSourceID; }
	@JsonProperty("mediaSourceId")
	public void setMediaSourceID(String value) { this.mediaSourceID = value; }

	@JsonProperty("remoteSource")
	public Boolean getRemoteSource() { return remoteSource; }
	@JsonProperty("remoteSource")
	public void setRemoteSource(Boolean value) { this.remoteSource = value; }

	@JsonProperty("removedSamplesForAcceleration")
	public Double getRemovedSamplesForAcceleration() { return removedSamplesForAcceleration; }
	@JsonProperty("removedSamplesForAcceleration")
	public void setRemovedSamplesForAcceleration(Double value) { this.removedSamplesForAcceleration = value; }

	@JsonProperty("silentConcealedSamples")
	public Double getSilentConcealedSamples() { return silentConcealedSamples; }
	@JsonProperty("silentConcealedSamples")
	public void setSilentConcealedSamples(Double value) { this.silentConcealedSamples = value; }

	@JsonProperty("totalAudioEnergy")
	public Double getTotalAudioEnergy() { return totalAudioEnergy; }
	@JsonProperty("totalAudioEnergy")
	public void setTotalAudioEnergy(Double value) { this.totalAudioEnergy = value; }

	@JsonProperty("totalSamplesDuration")
	public Double getTotalSamplesDuration() { return totalSamplesDuration; }
	@JsonProperty("totalSamplesDuration")
	public void setTotalSamplesDuration(Double value) { this.totalSamplesDuration = value; }

	@JsonProperty("totalSamplesReceived")
	public Double getTotalSamplesReceived() { return totalSamplesReceived; }
	@JsonProperty("totalSamplesReceived")
	public void setTotalSamplesReceived(Double value) { this.totalSamplesReceived = value; }

	@JsonProperty("trackIdentifier")
	public String getTrackIdentifier() { return trackIdentifier; }
	@JsonProperty("trackIdentifier")
	public void setTrackIdentifier(String value) { this.trackIdentifier = value; }

	@JsonProperty("codecId")
	public String getCodecID() { return codecID; }
	@JsonProperty("codecId")
	public void setCodecID(String value) { this.codecID = value; }

	@JsonProperty("decoderImplementation")
	public String getDecoderImplementation() { return decoderImplementation; }
	@JsonProperty("decoderImplementation")
	public void setDecoderImplementation(String value) { this.decoderImplementation = value; }

	@JsonProperty("estimatedPlayoutTimestamp")
	public Double getEstimatedPlayoutTimestamp() { return estimatedPlayoutTimestamp; }
	@JsonProperty("estimatedPlayoutTimestamp")
	public void setEstimatedPlayoutTimestamp(Double value) { this.estimatedPlayoutTimestamp = value; }

	@JsonProperty("fecPacketsDiscarded")
	public Double getFECPacketsDiscarded() { return fecPacketsDiscarded; }
	@JsonProperty("fecPacketsDiscarded")
	public void setFECPacketsDiscarded(Double value) { this.fecPacketsDiscarded = value; }

	@JsonProperty("fecPacketsReceived")
	public Double getFECPacketsReceived() { return fecPacketsReceived; }
	@JsonProperty("fecPacketsReceived")
	public void setFECPacketsReceived(Double value) { this.fecPacketsReceived = value; }

	@JsonProperty("firCount")
	public Double getFirCount() { return firCount; }
	@JsonProperty("firCount")
	public void setFirCount(Double value) { this.firCount = value; }

	@JsonProperty("headerBytesReceived")
	public Double getHeaderBytesReceived() { return headerBytesReceived; }
	@JsonProperty("headerBytesReceived")
	public void setHeaderBytesReceived(Double value) { this.headerBytesReceived = value; }

	@JsonProperty("jitter")
	public Double getJitter() { return jitter; }
	@JsonProperty("jitter")
	public void setJitter(Double value) { this.jitter = value; }

	@JsonProperty("keyFramesDecoded")
	public Double getKeyFramesDecoded() { return keyFramesDecoded; }
	@JsonProperty("keyFramesDecoded")
	public void setKeyFramesDecoded(Double value) { this.keyFramesDecoded = value; }

	@JsonProperty("lastPacketReceivedTimestamp")
	public Double getLastPacketReceivedTimestamp() { return lastPacketReceivedTimestamp; }
	@JsonProperty("lastPacketReceivedTimestamp")
	public void setLastPacketReceivedTimestamp(Double value) { this.lastPacketReceivedTimestamp = value; }

	@JsonProperty("mediaType")
	public Kind getMediaType() { return mediaType; }
	@JsonProperty("mediaType")
	public void setMediaType(Kind value) { this.mediaType = value; }

	@JsonProperty("nackCount")
	public Double getNACKCount() { return nackCount; }
	@JsonProperty("nackCount")
	public void setNACKCount(Double value) { this.nackCount = value; }

	@JsonProperty("packetsLost")
	public Double getPacketsLost() { return packetsLost; }
	@JsonProperty("packetsLost")
	public void setPacketsLost(Double value) { this.packetsLost = value; }

	@JsonProperty("packetsReceived")
	public Double getPacketsReceived() { return packetsReceived; }
	@JsonProperty("packetsReceived")
	public void setPacketsReceived(Double value) { this.packetsReceived = value; }

	@JsonProperty("pliCount")
	public Double getPliCount() { return pliCount; }
	@JsonProperty("pliCount")
	public void setPliCount(Double value) { this.pliCount = value; }

	@JsonProperty("qpSum")
	public Double getQpSum() { return qpSum; }
	@JsonProperty("qpSum")
	public void setQpSum(Double value) { this.qpSum = value; }

	@JsonProperty("ssrc")
	public Double getSsrc() { return ssrc; }
	@JsonProperty("ssrc")
	public void setSsrc(Double value) { this.ssrc = value; }

	@JsonProperty("totalDecodeTime")
	public Double getTotalDecodeTime() { return totalDecodeTime; }
	@JsonProperty("totalDecodeTime")
	public void setTotalDecodeTime(Double value) { this.totalDecodeTime = value; }

	@JsonProperty("totalInterFrameDelay")
	public Double getTotalInterFrameDelay() { return totalInterFrameDelay; }
	@JsonProperty("totalInterFrameDelay")
	public void setTotalInterFrameDelay(Double value) { this.totalInterFrameDelay = value; }

	@JsonProperty("totalSquaredInterFrameDelay")
	public Double getTotalSquaredInterFrameDelay() { return totalSquaredInterFrameDelay; }
	@JsonProperty("totalSquaredInterFrameDelay")
	public void setTotalSquaredInterFrameDelay(Double value) { this.totalSquaredInterFrameDelay = value; }

	@JsonProperty("trackId")
	public String getTrackID() { return trackID; }
	@JsonProperty("trackId")
	public void setTrackID(String value) { this.trackID = value; }
}

// Timestamp.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.core.*;
		import com.fasterxml.jackson.databind.*;
		import com.fasterxml.jackson.databind.annotation.*;

@JsonDeserialize(using = Timestamp.Deserializer.class)
@JsonSerialize(using = Timestamp.Serializer.class)
public class Timestamp {
	public Double doubleValue;
	public String stringValue;

	static class Deserializer extends JsonDeserializer<Timestamp> {
		@Override
		public Timestamp deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
			Timestamp value = new Timestamp();
			switch (jsonParser.getCurrentToken()) {
				case VALUE_NUMBER_INT:
				case VALUE_NUMBER_FLOAT:
					value.doubleValue = jsonParser.readValueAs(Double.class);
					break;
				case VALUE_STRING:
					value.stringValue = jsonParser.readValueAs(String.class);
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

// ReceiverStatType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum ReceiverStatType {
	CANDIDATE_PAIR, INBOUND_RTP, LOCAL_CANDIDATE, REMOTE_CANDIDATE, TRACK;

	@JsonValue
	public String toValue() {
		switch (this) {
			case CANDIDATE_PAIR: return "candidate-pair";
			case INBOUND_RTP: return "inbound-rtp";
			case LOCAL_CANDIDATE: return "local-candidate";
			case REMOTE_CANDIDATE: return "remote-candidate";
			case TRACK: return "track";
		}
		return null;
	}

	@JsonCreator
	public static ReceiverStatType forValue(String value) throws IOException {
		if (value.equals("candidate-pair")) return CANDIDATE_PAIR;
		if (value.equals("inbound-rtp")) return INBOUND_RTP;
		if (value.equals("local-candidate")) return LOCAL_CANDIDATE;
		if (value.equals("remote-candidate")) return REMOTE_CANDIDATE;
		if (value.equals("track")) return TRACK;
		throw new IOException("Cannot deserialize ReceiverStatType");
	}
}

// SenderStatElement.java

package io.quicktype;

		import java.util.*;
		import com.fasterxml.jackson.annotation.*;

public class SenderStatElement {
	private Double audioLevel;
	private Double framesPerSecond;
	private Double height;
	private String id;
	private Kind kind;
	private Timestamp timestamp;
	private Double totalAudioEnergy;
	private Double totalSamplesDuration;
	private String trackIdentifier;
	private SenderStatType type;
	private Double width;
	private Double availableOutgoingBitrate;
	private Double bytesReceived;
	private Double bytesSent;
	private Double consentRequestsSent;
	private Double currentRoundTripTime;
	private String localCandidateID;
	private Boolean nominated;
	private Double priority;
	private String remoteCandidateID;
	private Double requestsReceived;
	private Double requestsSent;
	private Double responsesReceived;
	private Double responsesSent;
	private State state;
	private Double totalRoundTripTime;
	private String transportID;
	private Boolean writable;
	private CandidateType candidateType;
	private Boolean deleted;
	private String ip;
	private Boolean isRemote;
	private Double port;
	private Protocol protocol;
	private NetworkType networkType;
	private Double concealedSamples;
	private Double concealmentEvents;
	private Boolean detached;
	private Boolean ended;
	private Double frameHeight;
	private Double framesDecoded;
	private Double framesDropped;
	private Double framesReceived;
	private Double framesSent;
	private Double frameWidth;
	private Double hugeFramesSent;
	private Double insertedSamplesForDeceleration;
	private Double jitterBufferDelay;
	private Double jitterBufferEmittedCount;
	private String mediaSourceID;
	private Boolean remoteSource;
	private Double removedSamplesForAcceleration;
	private Double silentConcealedSamples;
	private Double totalSamplesReceived;
	private String codecID;
	private String encoderImplementation;
	private Double firCount;
	private Double framesEncoded;
	private Double headerBytesSent;
	private Double keyFramesEncoded;
	private Kind mediaType;
	private Double nackCount;
	private Double packetsSent;
	private Double pliCount;
	private Double qpSum;
	private QualityLimitationReason qualityLimitationReason;
	private Double qualityLimitationResolutionChanges;
	private String remoteID;
	private Double retransmittedBytesSent;
	private Double retransmittedPacketsSent;
	private Double ssrc;
	private Double totalEncodedBytesTarget;
	private Double totalEncodeTime;
	private Double totalPacketSendDelay;
	private Double trackID;
	private Double jitter;
	private String localID;
	private Double packetsLost;
	private Double roundTripTime;

	@JsonProperty("audioLevel")
	public Double getAudioLevel() { return audioLevel; }
	@JsonProperty("audioLevel")
	public void setAudioLevel(Double value) { this.audioLevel = value; }

	@JsonProperty("framesPerSecond")
	public Double getFramesPerSecond() { return framesPerSecond; }
	@JsonProperty("framesPerSecond")
	public void setFramesPerSecond(Double value) { this.framesPerSecond = value; }

	@JsonProperty("height")
	public Double getHeight() { return height; }
	@JsonProperty("height")
	public void setHeight(Double value) { this.height = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("timestamp")
	public Timestamp getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(Timestamp value) { this.timestamp = value; }

	@JsonProperty("totalAudioEnergy")
	public Double getTotalAudioEnergy() { return totalAudioEnergy; }
	@JsonProperty("totalAudioEnergy")
	public void setTotalAudioEnergy(Double value) { this.totalAudioEnergy = value; }

	@JsonProperty("totalSamplesDuration")
	public Double getTotalSamplesDuration() { return totalSamplesDuration; }
	@JsonProperty("totalSamplesDuration")
	public void setTotalSamplesDuration(Double value) { this.totalSamplesDuration = value; }

	@JsonProperty("trackIdentifier")
	public String getTrackIdentifier() { return trackIdentifier; }
	@JsonProperty("trackIdentifier")
	public void setTrackIdentifier(String value) { this.trackIdentifier = value; }

	@JsonProperty("type")
	public SenderStatType getType() { return type; }
	@JsonProperty("type")
	public void setType(SenderStatType value) { this.type = value; }

	@JsonProperty("width")
	public Double getWidth() { return width; }
	@JsonProperty("width")
	public void setWidth(Double value) { this.width = value; }

	@JsonProperty("availableOutgoingBitrate")
	public Double getAvailableOutgoingBitrate() { return availableOutgoingBitrate; }
	@JsonProperty("availableOutgoingBitrate")
	public void setAvailableOutgoingBitrate(Double value) { this.availableOutgoingBitrate = value; }

	@JsonProperty("bytesReceived")
	public Double getBytesReceived() { return bytesReceived; }
	@JsonProperty("bytesReceived")
	public void setBytesReceived(Double value) { this.bytesReceived = value; }

	@JsonProperty("bytesSent")
	public Double getBytesSent() { return bytesSent; }
	@JsonProperty("bytesSent")
	public void setBytesSent(Double value) { this.bytesSent = value; }

	@JsonProperty("consentRequestsSent")
	public Double getConsentRequestsSent() { return consentRequestsSent; }
	@JsonProperty("consentRequestsSent")
	public void setConsentRequestsSent(Double value) { this.consentRequestsSent = value; }

	@JsonProperty("currentRoundTripTime")
	public Double getCurrentRoundTripTime() { return currentRoundTripTime; }
	@JsonProperty("currentRoundTripTime")
	public void setCurrentRoundTripTime(Double value) { this.currentRoundTripTime = value; }

	@JsonProperty("localCandidateId")
	public String getLocalCandidateID() { return localCandidateID; }
	@JsonProperty("localCandidateId")
	public void setLocalCandidateID(String value) { this.localCandidateID = value; }

	@JsonProperty("nominated")
	public Boolean getNominated() { return nominated; }
	@JsonProperty("nominated")
	public void setNominated(Boolean value) { this.nominated = value; }

	@JsonProperty("priority")
	public Double getPriority() { return priority; }
	@JsonProperty("priority")
	public void setPriority(Double value) { this.priority = value; }

	@JsonProperty("remoteCandidateId")
	public String getRemoteCandidateID() { return remoteCandidateID; }
	@JsonProperty("remoteCandidateId")
	public void setRemoteCandidateID(String value) { this.remoteCandidateID = value; }

	@JsonProperty("requestsReceived")
	public Double getRequestsReceived() { return requestsReceived; }
	@JsonProperty("requestsReceived")
	public void setRequestsReceived(Double value) { this.requestsReceived = value; }

	@JsonProperty("requestsSent")
	public Double getRequestsSent() { return requestsSent; }
	@JsonProperty("requestsSent")
	public void setRequestsSent(Double value) { this.requestsSent = value; }

	@JsonProperty("responsesReceived")
	public Double getResponsesReceived() { return responsesReceived; }
	@JsonProperty("responsesReceived")
	public void setResponsesReceived(Double value) { this.responsesReceived = value; }

	@JsonProperty("responsesSent")
	public Double getResponsesSent() { return responsesSent; }
	@JsonProperty("responsesSent")
	public void setResponsesSent(Double value) { this.responsesSent = value; }

	@JsonProperty("state")
	public State getState() { return state; }
	@JsonProperty("state")
	public void setState(State value) { this.state = value; }

	@JsonProperty("totalRoundTripTime")
	public Double getTotalRoundTripTime() { return totalRoundTripTime; }
	@JsonProperty("totalRoundTripTime")
	public void setTotalRoundTripTime(Double value) { this.totalRoundTripTime = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("writable")
	public Boolean getWritable() { return writable; }
	@JsonProperty("writable")
	public void setWritable(Boolean value) { this.writable = value; }

	@JsonProperty("candidateType")
	public CandidateType getCandidateType() { return candidateType; }
	@JsonProperty("candidateType")
	public void setCandidateType(CandidateType value) { this.candidateType = value; }

	@JsonProperty("deleted")
	public Boolean getDeleted() { return deleted; }
	@JsonProperty("deleted")
	public void setDeleted(Boolean value) { this.deleted = value; }

	@JsonProperty("ip")
	public String getIP() { return ip; }
	@JsonProperty("ip")
	public void setIP(String value) { this.ip = value; }

	@JsonProperty("isRemote")
	public Boolean getIsRemote() { return isRemote; }
	@JsonProperty("isRemote")
	public void setIsRemote(Boolean value) { this.isRemote = value; }

	@JsonProperty("port")
	public Double getPort() { return port; }
	@JsonProperty("port")
	public void setPort(Double value) { this.port = value; }

	@JsonProperty("protocol")
	public Protocol getProtocol() { return protocol; }
	@JsonProperty("protocol")
	public void setProtocol(Protocol value) { this.protocol = value; }

	@JsonProperty("networkType")
	public NetworkType getNetworkType() { return networkType; }
	@JsonProperty("networkType")
	public void setNetworkType(NetworkType value) { this.networkType = value; }

	@JsonProperty("concealedSamples")
	public Double getConcealedSamples() { return concealedSamples; }
	@JsonProperty("concealedSamples")
	public void setConcealedSamples(Double value) { this.concealedSamples = value; }

	@JsonProperty("concealmentEvents")
	public Double getConcealmentEvents() { return concealmentEvents; }
	@JsonProperty("concealmentEvents")
	public void setConcealmentEvents(Double value) { this.concealmentEvents = value; }

	@JsonProperty("detached")
	public Boolean getDetached() { return detached; }
	@JsonProperty("detached")
	public void setDetached(Boolean value) { this.detached = value; }

	@JsonProperty("ended")
	public Boolean getEnded() { return ended; }
	@JsonProperty("ended")
	public void setEnded(Boolean value) { this.ended = value; }

	@JsonProperty("frameHeight")
	public Double getFrameHeight() { return frameHeight; }
	@JsonProperty("frameHeight")
	public void setFrameHeight(Double value) { this.frameHeight = value; }

	@JsonProperty("framesDecoded")
	public Double getFramesDecoded() { return framesDecoded; }
	@JsonProperty("framesDecoded")
	public void setFramesDecoded(Double value) { this.framesDecoded = value; }

	@JsonProperty("framesDropped")
	public Double getFramesDropped() { return framesDropped; }
	@JsonProperty("framesDropped")
	public void setFramesDropped(Double value) { this.framesDropped = value; }

	@JsonProperty("framesReceived")
	public Double getFramesReceived() { return framesReceived; }
	@JsonProperty("framesReceived")
	public void setFramesReceived(Double value) { this.framesReceived = value; }

	@JsonProperty("framesSent")
	public Double getFramesSent() { return framesSent; }
	@JsonProperty("framesSent")
	public void setFramesSent(Double value) { this.framesSent = value; }

	@JsonProperty("frameWidth")
	public Double getFrameWidth() { return frameWidth; }
	@JsonProperty("frameWidth")
	public void setFrameWidth(Double value) { this.frameWidth = value; }

	@JsonProperty("hugeFramesSent")
	public Double getHugeFramesSent() { return hugeFramesSent; }
	@JsonProperty("hugeFramesSent")
	public void setHugeFramesSent(Double value) { this.hugeFramesSent = value; }

	@JsonProperty("insertedSamplesForDeceleration")
	public Double getInsertedSamplesForDeceleration() { return insertedSamplesForDeceleration; }
	@JsonProperty("insertedSamplesForDeceleration")
	public void setInsertedSamplesForDeceleration(Double value) { this.insertedSamplesForDeceleration = value; }

	@JsonProperty("jitterBufferDelay")
	public Double getJitterBufferDelay() { return jitterBufferDelay; }
	@JsonProperty("jitterBufferDelay")
	public void setJitterBufferDelay(Double value) { this.jitterBufferDelay = value; }

	@JsonProperty("jitterBufferEmittedCount")
	public Double getJitterBufferEmittedCount() { return jitterBufferEmittedCount; }
	@JsonProperty("jitterBufferEmittedCount")
	public void setJitterBufferEmittedCount(Double value) { this.jitterBufferEmittedCount = value; }

	@JsonProperty("mediaSourceId")
	public String getMediaSourceID() { return mediaSourceID; }
	@JsonProperty("mediaSourceId")
	public void setMediaSourceID(String value) { this.mediaSourceID = value; }

	@JsonProperty("remoteSource")
	public Boolean getRemoteSource() { return remoteSource; }
	@JsonProperty("remoteSource")
	public void setRemoteSource(Boolean value) { this.remoteSource = value; }

	@JsonProperty("removedSamplesForAcceleration")
	public Double getRemovedSamplesForAcceleration() { return removedSamplesForAcceleration; }
	@JsonProperty("removedSamplesForAcceleration")
	public void setRemovedSamplesForAcceleration(Double value) { this.removedSamplesForAcceleration = value; }

	@JsonProperty("silentConcealedSamples")
	public Double getSilentConcealedSamples() { return silentConcealedSamples; }
	@JsonProperty("silentConcealedSamples")
	public void setSilentConcealedSamples(Double value) { this.silentConcealedSamples = value; }

	@JsonProperty("totalSamplesReceived")
	public Double getTotalSamplesReceived() { return totalSamplesReceived; }
	@JsonProperty("totalSamplesReceived")
	public void setTotalSamplesReceived(Double value) { this.totalSamplesReceived = value; }

	@JsonProperty("codecId")
	public String getCodecID() { return codecID; }
	@JsonProperty("codecId")
	public void setCodecID(String value) { this.codecID = value; }

	@JsonProperty("encoderImplementation")
	public String getEncoderImplementation() { return encoderImplementation; }
	@JsonProperty("encoderImplementation")
	public void setEncoderImplementation(String value) { this.encoderImplementation = value; }

	@JsonProperty("firCount")
	public Double getFirCount() { return firCount; }
	@JsonProperty("firCount")
	public void setFirCount(Double value) { this.firCount = value; }

	@JsonProperty("framesEncoded")
	public Double getFramesEncoded() { return framesEncoded; }
	@JsonProperty("framesEncoded")
	public void setFramesEncoded(Double value) { this.framesEncoded = value; }

	@JsonProperty("headerBytesSent")
	public Double getHeaderBytesSent() { return headerBytesSent; }
	@JsonProperty("headerBytesSent")
	public void setHeaderBytesSent(Double value) { this.headerBytesSent = value; }

	@JsonProperty("keyFramesEncoded")
	public Double getKeyFramesEncoded() { return keyFramesEncoded; }
	@JsonProperty("keyFramesEncoded")
	public void setKeyFramesEncoded(Double value) { this.keyFramesEncoded = value; }

	@JsonProperty("mediaType")
	public Kind getMediaType() { return mediaType; }
	@JsonProperty("mediaType")
	public void setMediaType(Kind value) { this.mediaType = value; }

	@JsonProperty("nackCount")
	public Double getNACKCount() { return nackCount; }
	@JsonProperty("nackCount")
	public void setNACKCount(Double value) { this.nackCount = value; }

	@JsonProperty("packetsSent")
	public Double getPacketsSent() { return packetsSent; }
	@JsonProperty("packetsSent")
	public void setPacketsSent(Double value) { this.packetsSent = value; }

	@JsonProperty("pliCount")
	public Double getPliCount() { return pliCount; }
	@JsonProperty("pliCount")
	public void setPliCount(Double value) { this.pliCount = value; }

	@JsonProperty("qpSum")
	public Double getQpSum() { return qpSum; }
	@JsonProperty("qpSum")
	public void setQpSum(Double value) { this.qpSum = value; }

	@JsonProperty("qualityLimitationReason")
	public QualityLimitationReason getQualityLimitationReason() { return qualityLimitationReason; }
	@JsonProperty("qualityLimitationReason")
	public void setQualityLimitationReason(QualityLimitationReason value) { this.qualityLimitationReason = value; }

	@JsonProperty("qualityLimitationResolutionChanges")
	public Double getQualityLimitationResolutionChanges() { return qualityLimitationResolutionChanges; }
	@JsonProperty("qualityLimitationResolutionChanges")
	public void setQualityLimitationResolutionChanges(Double value) { this.qualityLimitationResolutionChanges = value; }

	@JsonProperty("remoteId")
	public String getRemoteID() { return remoteID; }
	@JsonProperty("remoteId")
	public void setRemoteID(String value) { this.remoteID = value; }

	@JsonProperty("retransmittedBytesSent")
	public Double getRetransmittedBytesSent() { return retransmittedBytesSent; }
	@JsonProperty("retransmittedBytesSent")
	public void setRetransmittedBytesSent(Double value) { this.retransmittedBytesSent = value; }

	@JsonProperty("retransmittedPacketsSent")
	public Double getRetransmittedPacketsSent() { return retransmittedPacketsSent; }
	@JsonProperty("retransmittedPacketsSent")
	public void setRetransmittedPacketsSent(Double value) { this.retransmittedPacketsSent = value; }

	@JsonProperty("ssrc")
	public Double getSsrc() { return ssrc; }
	@JsonProperty("ssrc")
	public void setSsrc(Double value) { this.ssrc = value; }

	@JsonProperty("totalEncodedBytesTarget")
	public Double getTotalEncodedBytesTarget() { return totalEncodedBytesTarget; }
	@JsonProperty("totalEncodedBytesTarget")
	public void setTotalEncodedBytesTarget(Double value) { this.totalEncodedBytesTarget = value; }

	@JsonProperty("totalEncodeTime")
	public Double getTotalEncodeTime() { return totalEncodeTime; }
	@JsonProperty("totalEncodeTime")
	public void setTotalEncodeTime(Double value) { this.totalEncodeTime = value; }

	@JsonProperty("totalPacketSendDelay")
	public Double getTotalPacketSendDelay() { return totalPacketSendDelay; }
	@JsonProperty("totalPacketSendDelay")
	public void setTotalPacketSendDelay(Double value) { this.totalPacketSendDelay = value; }

	@JsonProperty("trackId")
	public Double getTrackID() { return trackID; }
	@JsonProperty("trackId")
	public void setTrackID(Double value) { this.trackID = value; }

	@JsonProperty("jitter")
	public Double getJitter() { return jitter; }
	@JsonProperty("jitter")
	public void setJitter(Double value) { this.jitter = value; }

	@JsonProperty("localId")
	public String getLocalID() { return localID; }
	@JsonProperty("localId")
	public void setLocalID(String value) { this.localID = value; }

	@JsonProperty("packetsLost")
	public Double getPacketsLost() { return packetsLost; }
	@JsonProperty("packetsLost")
	public void setPacketsLost(Double value) { this.packetsLost = value; }

	@JsonProperty("roundTripTime")
	public Double getRoundTripTime() { return roundTripTime; }
	@JsonProperty("roundTripTime")
	public void setRoundTripTime(Double value) { this.roundTripTime = value; }
}

// SenderStatType.java

package io.quicktype;

		import java.util.*;
		import java.io.IOException;
		import com.fasterxml.jackson.annotation.*;

public enum SenderStatType {
	CANDIDATE_PAIR, LOCAL_CANDIDATE, MEDIA_SOURCE, OUTBOUND_RTP, REMOTE_CANDIDATE, REMOTE_INBOUND_RTP, TRACK;

	@JsonValue
	public String toValue() {
		switch (this) {
			case CANDIDATE_PAIR: return "candidate-pair";
			case LOCAL_CANDIDATE: return "local-candidate";
			case MEDIA_SOURCE: return "media-source";
			case OUTBOUND_RTP: return "outbound-rtp";
			case REMOTE_CANDIDATE: return "remote-candidate";
			case REMOTE_INBOUND_RTP: return "remote-inbound-rtp";
			case TRACK: return "track";
		}
		return null;
	}

	@JsonCreator
	public static SenderStatType forValue(String value) throws IOException {
		if (value.equals("candidate-pair")) return CANDIDATE_PAIR;
		if (value.equals("local-candidate")) return LOCAL_CANDIDATE;
		if (value.equals("media-source")) return MEDIA_SOURCE;
		if (value.equals("outbound-rtp")) return OUTBOUND_RTP;
		if (value.equals("remote-candidate")) return REMOTE_CANDIDATE;
		if (value.equals("remote-inbound-rtp")) return REMOTE_INBOUND_RTP;
		if (value.equals("track")) return TRACK;
		throw new IOException("Cannot deserialize SenderStatType");
	}
}
