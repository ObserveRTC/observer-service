package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.webrtcstat.samples.SampleDescription;
import java.util.function.BiConsumer;

public class SampleDescriptionUpdater implements BiConsumer<SampleDescription, Integer> {

	private final boolean derivativeUpdate;

	public SampleDescriptionUpdater() {
		this(false);
	}

	public SampleDescriptionUpdater(boolean derivativeUpdate) {
		this.derivativeUpdate = derivativeUpdate;
	}

	@Override
	public void accept(SampleDescription result, Integer value) {
		if (derivativeUpdate) {
			if (result.last == null) {
				result.presented += 1;
				result.last = value;
				return;
			}
			Integer newValue = value - result.last;
			result.last = value;
			value = newValue;
		}
		result.presented += 1;
		if (result.max == null || result.max < value) {
			result.max = value;
		}
		if (result.min == null || value < result.min) {
			result.min = value;
		}
		result.sum += value;
	}
}
