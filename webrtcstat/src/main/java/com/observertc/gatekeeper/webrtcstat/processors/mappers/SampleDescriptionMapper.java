package com.observertc.gatekeeper.webrtcstat.processors.mappers;

import com.observertc.gatekeeper.webrtcstat.samples.SampleDescription;


@SuppressWarnings({"WeakerAccess", "unused"})
public class SampleDescriptionMapper extends JsonToPOJOMapper<SampleDescription> {

	public SampleDescriptionMapper() {
		super(SampleDescription.class);
	}
}