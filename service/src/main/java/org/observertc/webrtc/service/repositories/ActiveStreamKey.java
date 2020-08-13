package org.observertc.webrtc.service.repositories;

import java.util.UUID;
import org.jooq.lambda.tuple.Tuple2;
import org.observertc.webrtc.common.UUIDAdapter;

/**
 * This class is a emporary collector class
 * for stuff we use for either on-the fly debug
 * and no place or when we don't know where to place
 * the particular functions yet.
 * <p>
 * Please always do static for that function and give a comment
 * for later decide where to put that particular function
 */
public class ActiveStreamKey extends Tuple2<UUID, Long> {

	public ActiveStreamKey(UUID observerUUID, Long SSRC) {
		super(observerUUID, SSRC);
	}

	public ActiveStreamKey(byte[] observerUUIDBytes, Long SSRC) {
		super(UUIDAdapter.toUUIDOrDefault(observerUUIDBytes, null), SSRC);
	}

	public ActiveStreamKey(Tuple2<UUID, Long> tuple) {
		super(tuple);
	}

	public byte[] getObserverUUIDBytes() {
		return UUIDAdapter.toBytesOrDefault(this.v1, null);
	}

	public UUID getObserverUUID() {
		return this.v1;
	}

	public Long getSSRC() {
		return this.v2;
	}
}
