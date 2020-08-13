package org.observertc.webrtc.service.dto;

import io.micronaut.core.annotation.Introspected;
import org.observertc.webrtc.service.jooq.tables.records.ActivestreamsRecord;

@Introspected
public class ActiveStreamDTO extends ActivestreamsRecord {
	/**
	 * Create a detached PeerconnectionsRecord
	 */
	public ActiveStreamDTO() {
		super();
	}

	public ActiveStreamDTO(byte[] observeruuid, Long ssrc, byte[] calluuid) {
		super(observeruuid, ssrc, calluuid);
	}
}
