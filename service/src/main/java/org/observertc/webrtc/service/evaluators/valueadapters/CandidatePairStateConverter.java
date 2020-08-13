package org.observertc.webrtc.service.evaluators.valueadapters;

import org.apache.commons.lang3.NotImplementedException;
import org.observertc.webrtc.common.reports.CandidatePairState;
import org.observertc.webrtc.service.dto.webextrapp.State;

public class CandidatePairStateConverter {

	public static CandidatePairState fromState(State state) {
		if (state == null) {
			return null;
		}
		switch (state) {
			case FAILED:
				return CandidatePairState.FAILED;
			case SUCCEEDED:
				return CandidatePairState.SUCCEEDED;
			case FROZEN:
				return CandidatePairState.FROZEN;
			case WAITING:
				return CandidatePairState.WAITING;
			case IN_PROGRESS:
				return CandidatePairState.IN_PROGRESS;
			default:
				throw new NotImplementedException("Not implemented conversation for state  " + state.name());
		}
	}
}
