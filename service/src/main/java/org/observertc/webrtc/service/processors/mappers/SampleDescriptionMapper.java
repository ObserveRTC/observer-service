package org.observertc.webrtc.service.processors.mappers;

import org.observertc.webrtc.service.samples.MediaStreamAggregateRecord;


@SuppressWarnings({"WeakerAccess", "unused"})
public class SampleDescriptionMapper extends JsonToPOJOMapper<MediaStreamAggregateRecord> {

	public SampleDescriptionMapper() {
		super(MediaStreamAggregateRecord.class);
	}
}