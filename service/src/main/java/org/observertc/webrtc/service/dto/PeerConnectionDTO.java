package org.observertc.webrtc.service.dto;

import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;
import org.observertc.webrtc.service.jooq.enums.PeerconnectionsState;
import org.observertc.webrtc.service.jooq.tables.records.PeerconnectionsRecord;

@Introspected
public class PeerConnectionDTO extends PeerconnectionsRecord {
	/**
	 * Create a detached PeerconnectionsRecord
	 */
	public PeerConnectionDTO() {
		super();
	}

	/**
	 * Create a detached, initialised PeerconnectionsRecord
	 */
	public PeerConnectionDTO(byte[] peerconnectionuuid, LocalDateTime reported, LocalDateTime updated, PeerconnectionsState state, String browserid, String timezone, byte[] calluuid, byte[] observeruuid) {
		super(peerconnectionuuid, reported, updated, state, browserid, timezone, calluuid, observeruuid);
	}
}
