package org.observertc.webrtc.observer.service.processors.mappers;

import org.observertc.webrtc.observer.service.samples.SampleDescription;


@SuppressWarnings({"WeakerAccess", "unused"})
public class SampleDescriptionMapper extends JsonToPOJOMapper<SampleDescription> {

	public SampleDescriptionMapper() {
		super(SampleDescription.class);
	}
}