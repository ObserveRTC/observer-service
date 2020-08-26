package org.observertc.webrtc.observer.evaluators.valueadapters;

import java.util.NoSuchElementException;
import org.observertc.webrtc.common.reports.CandidateType;
import org.observertc.webrtc.observer.dto.webextrapp.CandidateTypeEnum;

public class CandidateTypeConverter {

	public static CandidateType from(CandidateTypeEnum candidateTypeEnum) {
		if (candidateTypeEnum == null) {
			return null;
		}
		switch (candidateTypeEnum) {
			case HOST:
				return CandidateType.HOST;
			case PRFLX:
				return CandidateType.PRFLX;
			case RELAY:
				return CandidateType.RELAY;
			case SRFLX:
				return CandidateType.SRFLX;
			default:
				throw new NoSuchElementException("NetworkType for " + candidateTypeEnum.name() + " is not implemented");
		}
	}

}
