package org.observertc.webrtc.observer.dto.v20200114;

import java.util.function.Function;
import org.apache.avro.generic.GenericRecord;

public class AvroConverter implements Function<GenericRecord, PeerConnectionSample> {

	@Override
	public PeerConnectionSample apply(GenericRecord record) {
		PeerConnectionSample result = new PeerConnectionSample();
		return result;
	}

	private PeerConnectionSample.ICECandidatePair getCandidatePair(GenericRecord record) {
		return null;
	}

}
