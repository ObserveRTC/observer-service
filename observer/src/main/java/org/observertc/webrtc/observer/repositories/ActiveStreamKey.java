package org.observertc.webrtc.observer.repositories;

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

	public ActiveStreamKey(UUID serviceUUID, Long SSRC) {
		super(serviceUUID, SSRC);
	}

	public ActiveStreamKey(byte[] serviceUUIDBytes, Long SSRC) {
		super(UUIDAdapter.toUUIDOrDefault(serviceUUIDBytes, null), SSRC);
	}

	public ActiveStreamKey(Tuple2<UUID, Long> tuple) {
		super(tuple);
	}

	public byte[] getServiceUUIDBytes() {
		return UUIDAdapter.toBytesOrDefault(this.v1, null);
	}

	public UUID getServiceUUID() {
		return this.v1;
	}

	public Long getSSRC() {
		return this.v2;
	}
}
